package org.dsinczak.paymentsprocessing.notification.twoPhasePublisher;


import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
public class DbEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(nullable = false)
    String event;

    public DbEvent(String event) {
        this.event = event;
    }

}
