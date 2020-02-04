package org.dsinczak.paymentsprocessing.readModel;

import io.vavr.control.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public interface PaymentViewRepository extends JpaRepository<PaymentView, Long> {

    Option<PaymentView> findByPaymentId(UUID paymentId);

}
