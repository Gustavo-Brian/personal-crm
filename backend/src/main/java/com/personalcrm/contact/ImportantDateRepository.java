package com.personalcrm.contact;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportantDateRepository extends JpaRepository<ImportantDate, Long> {

    List<ImportantDate> findByContactIdOrderByDateAscTitleAsc(Long contactId);

    Optional<ImportantDate> findByIdAndContactId(Long id, Long contactId);
}
