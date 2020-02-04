package org.dsinczak.paymentsprocessing.notification.twoPhasePublisher;

import io.vavr.control.Option;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DbEventRepository extends JpaRepository<DbEvent, Long> {

    Option<DbEvent> findTop1ByOrderByIdAsc();

}
