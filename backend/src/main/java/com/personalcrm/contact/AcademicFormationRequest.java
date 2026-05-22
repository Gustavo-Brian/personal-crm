package com.personalcrm.contact;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record AcademicFormationRequest(
        @NotBlank(message = "Institution is required")
        @Size(max = 160, message = "Institution must have at most 160 characters")
        String institution,

        @Size(max = 120, message = "Degree must have at most 120 characters")
        String degree,

        @Size(max = 160, message = "Field of study must have at most 160 characters")
        String fieldOfStudy,

        LocalDate startDate,

        LocalDate endDate,

        @Size(max = 1000, message = "Description must have at most 1000 characters")
        String description
) {
}
