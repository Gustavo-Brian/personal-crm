package com.personalcrm.appointment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByOwnerIdOrderByStartsAtAscTitleAsc(Long ownerId);

    List<Appointment> findByOwnerIdAndStartsAtGreaterThanEqualOrderByStartsAtAscTitleAsc(
            Long ownerId,
            LocalDateTime startsAt
    );

    Optional<Appointment> findByIdAndOwnerId(Long id, Long ownerId);
}
