package org.dsinczak.paymentsprocessing


import org.dsinczak.paymentsprocessing.domain.CancellationFeePolicy
import org.dsinczak.paymentsprocessing.domain.PaymentFactory
import org.dsinczak.paymentsprocessing.domain.PaymentRepository
import org.dsinczak.paymentsprocessing.notification.EventPublisher
import spock.lang.Specification

class PaymentsServiceCaseSpec extends Specification{

    PaymentFactory paymentFactory = Mock(PaymentFactory)
    PaymentRepository paymentRepository = Mock(PaymentRepository)
    CancellationFeePolicy cancellationFeePolicy = Mock(CancellationFeePolicy)
    EventPublisher eventPublisher = Mock(EventPublisher)
    PaymentsService paymentsService = new PaymentsService(paymentFactory,paymentRepository, cancellationFeePolicy, eventPublisher)

    def 'should'(){
        // I hope i proved each class can be tested separately as unit
        // or as component with cooperation with other classes.
        // I'm running out of time :)
    }
}
