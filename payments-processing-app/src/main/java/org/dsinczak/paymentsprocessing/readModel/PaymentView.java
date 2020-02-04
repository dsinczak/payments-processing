package org.dsinczak.paymentsprocessing.readModel;

import lombok.Getter;
import lombok.ToString;
import org.dsinczak.paymentsprocessing.shared.MonetaryAmountConverter;
import org.hibernate.annotations.Immutable;

import javax.money.MonetaryAmount;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

/**
 * This is only payment view entity.
 */
@Entity
@Immutable
@Table(name = "payment")
@Getter
@ToString
public class PaymentView {

    @Id
    private Long id;
    private UUID paymentId;
    @Convert(converter = MonetaryAmountConverter.class)
    private MonetaryAmount cancellationFee;

}
