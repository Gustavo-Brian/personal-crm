package com.personalcrm.contact;

import jakarta.validation.constraints.Size;

public record ContactAddressRequest(
        @Size(max = 255, message = "Street must have at most 255 characters")
        String street,

        @Size(max = 120, message = "City must have at most 120 characters")
        String city,

        @Size(max = 120, message = "State must have at most 120 characters")
        String state,

        @Size(max = 40, message = "Postal code must have at most 40 characters")
        String postalCode,

        @Size(max = 120, message = "Country must have at most 120 characters")
        String country
) {
}
