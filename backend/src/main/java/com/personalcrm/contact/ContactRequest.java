package com.personalcrm.contact;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ContactRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 160, message = "Name must have at most 160 characters")
        String name,

        @Size(max = 160, message = "Organization must have at most 160 characters")
        String organization,

        @Size(max = 120, message = "Job title must have at most 120 characters")
        String jobTitle,

        LocalDate birthday
) {
}
