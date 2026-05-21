package com.personalcrm.contact;

public record ContactPhoneResponse(
        String label,
        String number
) {

    public static ContactPhoneResponse from(ContactPhoneNumber phoneNumber) {
        return new ContactPhoneResponse(
                phoneNumber.getLabel(),
                phoneNumber.getNumber()
        );
    }
}
