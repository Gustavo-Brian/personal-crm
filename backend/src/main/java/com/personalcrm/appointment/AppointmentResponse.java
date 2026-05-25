package com.personalcrm.appointment;

import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long contactId,
        String title,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        String location,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static AppointmentResponse from(Appointment appointment) {
        Long contactId = appointment.getContact() == null ? null : appointment.getContact().getId();
        return new AppointmentResponse(
                appointment.getId(),
                contactId,
                appointment.getTitle(),
                appointment.getStartsAt(),
                appointment.getEndsAt(),
                appointment.getLocation(),
                appointment.getDescription(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}
