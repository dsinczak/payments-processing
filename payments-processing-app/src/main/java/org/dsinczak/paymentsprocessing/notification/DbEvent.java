package org.dsinczak.paymentsprocessing.notification;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
public class DbEvent {

    @Id @GeneratedValue(strategy= GenerationType.AUTO)
    long id;

    UUID eventId;

    String event;

    LocalDateTime send;

    public DbEvent(UUID eventId, String event, LocalDateTime send) {
        this.eventId = eventId;
        this.event = event;
        this.send = send;
    }

    public DbEvent(UUID eventId, String event) {
        this.eventId = eventId;
        this.event = event;
        this.send = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DbEvent)) return false;
        DbEvent dbEvent = (DbEvent) o;
        return Objects.equals(getEventId(), dbEvent.getEventId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEventId());
    }
}
