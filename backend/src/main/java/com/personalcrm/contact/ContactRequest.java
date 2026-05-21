package com.personalcrm.contact;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record ContactRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 160, message = "Name must have at most 160 characters")
        String name,

        @Size(max = 160, message = "Organization must have at most 160 characters")
        String organization,

        @Size(max = 120, message = "Job title must have at most 120 characters")
        String jobTitle,

        LocalDate birthday,

        @Valid
        @Size(max = 20, message = "Phone numbers must have at most 20 entries")
        List<@Valid @NotNull(message = "Phone number entry is required") ContactPhoneRequest> phoneNumbers,

        @Valid
        @Size(max = 20, message = "Email addresses must have at most 20 entries")
        List<@Valid @NotNull(message = "Email address entry is required") ContactEmailRequest> emailAddresses,

        @Valid
        ContactAddressRequest address,

        @Size(max = 2000, message = "Notes must have at most 2000 characters")
        String notes
) {
}
