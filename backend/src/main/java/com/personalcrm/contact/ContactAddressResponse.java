package com.personalcrm.contact;

public record ContactAddressResponse(
        String street,
        String city,
        String state,
        String postalCode,
        String country
) {

    public static ContactAddressResponse from(ContactAddress address) {
        if (address == null || address.isEmpty()) {
            return null;
        }

        return new ContactAddressResponse(
                address.getStreet(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry()
        );
    }
}
