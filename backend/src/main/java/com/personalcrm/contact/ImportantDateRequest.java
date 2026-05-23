package com.personalcrm.contact;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ImportantDateRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 160, message = "Title must have at most 160 characters")
        String title,

        @NotNull(message = "Date is required")
        LocalDate date,

        @NotNull(message = "Type is required")
        ImportantDateType type,

        @Size(max = 1000, message = "Description must have at most 1000 characters")
        String description
) {
}
