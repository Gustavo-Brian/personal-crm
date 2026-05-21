package com.personalcrm.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactEmailRequest(
        @Size(max = 80, message = "Email label must have at most 80 characters")
        String label,

        @NotBlank(message = "Email address is required")
        @Email(message = "Email address must be valid")
        @Size(max = 255, message = "Email address must have at most 255 characters")
        String email
) {
}
