package com.personalcrm.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCredentialsRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 120, message = "Name must have at most 120 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must have at most 255 characters")
        String email,

        @NotBlank(message = "Current password is required")
        @Size(min = 8, max = 72, message = "Current password must have between 8 and 72 characters")
        String currentPassword,

        @Size(min = 8, max = 72, message = "New password must have between 8 and 72 characters")
        String newPassword
) {
}
