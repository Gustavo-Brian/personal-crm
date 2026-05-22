package com.personalcrm.contact;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicFormationRepository extends JpaRepository<AcademicFormation, Long> {

    List<AcademicFormation> findByContactIdOrderByStartDateDescInstitutionAsc(Long contactId);

    Optional<AcademicFormation> findByIdAndContactId(Long id, Long contactId);
}
