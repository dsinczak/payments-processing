package org.dsinczak.paymentsprocessing.domain;

import lombok.*;

import javax.persistence.Embeddable;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Embeddable
public class Iban {
    String value;
}
