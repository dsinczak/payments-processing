package org.dsinczak.paymentsprocessing;

import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.dsinczak.paymentsprocessing.api.ErrorDto;
import org.dsinczak.paymentsprocessing.api.MoneyDto;
import org.dsinczak.paymentsprocessing.api.PaymentCancellationFeeDto;
import org.dsinczak.paymentsprocessing.api.PaymentDto;
import org.dsinczak.paymentsprocessing.readModel.PaymentViewRepository;
import org.dsinczak.paymentsprocessing.shared.ErrorMessage;
import org.dsinczak.paymentsprocessing.web.ErrorMessageRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static io.vavr.API.*;
import static io.vavr.Patterns.*;
import static org.dsinczak.paymentsprocessing.shared.ErrorMessage.error;

/**
 * TODO Add custom converter for Either[ErrorMessage, ?] so controller methods
 * return exact type instead of ResponseEntity<Object>.
 */
@Slf4j
@RestController
@RequestMapping("/payment")
public class PaymentsController {

    private final PaymentsService paymentsService;
    private final ErrorMessageRenderer errorMessageRenderer;
    private final PaymentViewRepository paymentViewRepository;

    @Autowired
    PaymentsController(PaymentsService paymentFactory, ErrorMessageRenderer errorMessageRenderer, PaymentViewRepository paymentViewRepository) {
        this.paymentsService = paymentFactory;
        this.errorMessageRenderer = errorMessageRenderer;
        this.paymentViewRepository = paymentViewRepository;
    }

    @PostMapping()
    ResponseEntity<Object> createPayment(@RequestBody PaymentDto paymentDto) {
        return Match(paymentsService.createNewPayment(paymentDto)).of(
                Case($Right($()), pid -> ok(pid.toString())),
                Case($Left($()), this::badRequest)
        );
    }

    @PutMapping("/{paymentId}/cancellation")
    ResponseEntity<Object> cancelPayment(@PathVariable String paymentId) {
        var cancellation = Try.of(() -> UUID.fromString(paymentId)).toEither()
                .mapLeft(th -> error("Payment ID {0} is not valid UUID", paymentId))
                .flatMap(paymentsService::cancelPayment);

        return Match(cancellation).of(
                Case($Right($()), ma -> ok(new MoneyDto(ma.getNumber().toString(), ma.getCurrency().getCurrencyCode()))),
                Case($Left($()), this::badRequest)
        );
    }

    // It should be possible to query all payments that aren't canceled as well as filter them by amount. Query should return payment IDs.
    // There should also be an option to query specific payment by ID, and it should return payment ID and cancelation fee.

    @GetMapping("/{paymentId}/cancellation")
    ResponseEntity<Object> getCancellationByPaymentId(@PathVariable String paymentId) {
        var dto = Try.of(() -> UUID.fromString(paymentId)).toOption()
                .flatMap(paymentViewRepository::findByPaymentId)
                .map(pv->new PaymentCancellationFeeDto(
                        pv.getPaymentId().toString(),
                        pv.getCancellationFee().getNumber().toString(),
                        pv.getCancellationFee().getCurrency().getCurrencyCode())
                );

        return Match(dto).of(
                Case($Some($()), this::ok),
                Case($None(), badRequest(error("Payment with id {0} not found", paymentId)))
        );
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorDto> exceptionHandler(Exception ex) {
        log.error("Uncaught exception, sending apologise to the client", ex);
        return new ResponseEntity<>(new ErrorDto("Something went wrong. We are very sorry for inconvenience."), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Object> ok(Object o) {
        return new ResponseEntity<>(o, HttpStatus.OK);
    }

    private ResponseEntity<Object> badRequest(Seq<ErrorMessage> em) {
        return new ResponseEntity<>(new ErrorDto(errorMessageRenderer.render(em).asJava()), HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Object> badRequest(ErrorMessage em) {
        return new ResponseEntity<>(new ErrorDto(errorMessageRenderer.render(em)), HttpStatus.BAD_REQUEST);
    }

}
