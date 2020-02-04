package org.dsinczak.paymentsprocessing.readModel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public interface PaymentViewRepository extends JpaRepository<PaymentView, Long> {

}
