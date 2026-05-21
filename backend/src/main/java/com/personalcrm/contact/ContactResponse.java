package com.personalcrm.contact;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ContactResponse(
        Long id,
        String name,
        String organization,
        String jobTitle,
        LocalDate birthday,
        List<ContactPhoneResponse> phoneNumbers,
        List<ContactEmailResponse> emailAddresses,
        ContactAddressResponse address,
        String notes,
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
                contact.getPhoneNumbers()
                        .stream()
                        .map(ContactPhoneResponse::from)
                        .toList(),
                contact.getEmailAddresses()
                        .stream()
                        .map(ContactEmailResponse::from)
                        .toList(),
                ContactAddressResponse.from(contact.getAddress()),
                contact.getNotes(),
                contact.getCreatedAt(),
                contact.getUpdatedAt()
        );
    }
}
