package com.personalcrm.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GroupRequest(
        @NotBlank(message = "Group name is required")
        @Size(max = 120, message = "Group name must have at most 120 characters")
        String name,

        @Size(max = 500, message = "Description must have at most 500 characters")
        String description,

        @Pattern(regexp = "^\\s*$|#[0-9A-Fa-f]{6}", message = "Color must use #RRGGBB format")
        String colorHex
) {
}
