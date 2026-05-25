package com.personalcrm.appointment;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record AppointmentRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 160, message = "Title must have at most 160 characters")
        String title,

        @NotNull(message = "Start time is required")
        LocalDateTime startsAt,

        LocalDateTime endsAt,

        @Size(max = 255, message = "Location must have at most 255 characters")
        String location,

        @Size(max = 1000, message = "Description must have at most 1000 characters")
        String description,

        Long contactId
) {

    @AssertTrue(message = "End time must be after start time")
    public boolean isEndTimeAfterStartTime() {
        return startsAt == null || endsAt == null || endsAt.isAfter(startsAt);
    }
}
