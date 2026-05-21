package com.personalcrm.contact;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ContactResponse(
        Long id,
        String name,
        String organization,
        String jobTitle,
        LocalDate birthday,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static ContactResponse from(Contact contact) {
        return new ContactResponse(
                contact.getId(),
                contact.getName(),
                contact.getOrganization(),
                contact.getJobTitle(),
                contact.getBirthday(),
                contact.getCreatedAt(),
                contact.getUpdatedAt()
        );
    }
}
