package org.dsinczak.paymentsprocessing.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DbEventRepository extends JpaRepository<DbEvent, Long> {
}
