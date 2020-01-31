package org.dsinczak.paymentsprocessing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/payment")
public class PaymentsController {

    @GetMapping
    String helloWorld() {
        log.info("Hello world");
        return "Hello";
    }
}
