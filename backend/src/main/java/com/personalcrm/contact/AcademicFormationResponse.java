package com.personalcrm.contact;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AcademicFormationResponse(
        Long id,
        Long contactId,
        String institution,
        String degree,
        String fieldOfStudy,
        LocalDate startDate,
        LocalDate endDate,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AcademicFormationResponse from(AcademicFormation formation) {
        return new AcademicFormationResponse(
                formation.getId(),
                formation.getContact().getId(),
                formation.getInstitution(),
                formation.getDegree(),
                formation.getFieldOfStudy(),
                formation.getStartDate(),
                formation.getEndDate(),
                formation.getDescription(),
                formation.getCreatedAt(),
                formation.getUpdatedAt()
        );
    }
}
