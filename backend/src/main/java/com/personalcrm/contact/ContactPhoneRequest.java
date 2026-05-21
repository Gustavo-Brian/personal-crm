package com.personalcrm.contact;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactPhoneRequest(
        @Size(max = 80, message = "Phone label must have at most 80 characters")
        String label,

        @NotBlank(message = "Phone number is required")
        @Size(max = 40, message = "Phone number must have at most 40 characters")
        String number
) {
}
