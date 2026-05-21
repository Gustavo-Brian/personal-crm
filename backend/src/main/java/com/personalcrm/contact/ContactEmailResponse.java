package com.personalcrm.contact;

public record ContactEmailResponse(
        String label,
        String email
) {

    public static ContactEmailResponse from(ContactEmailAddress emailAddress) {
        return new ContactEmailResponse(
                emailAddress.getLabel(),
                emailAddress.getEmail()
        );
    }
}
