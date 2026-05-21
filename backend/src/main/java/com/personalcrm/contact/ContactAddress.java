package com.personalcrm.contact;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ContactAddress {

    @Column(name = "address_street", length = 255)
    private String street;

    @Column(name = "address_city", length = 120)
    private String city;

    @Column(name = "address_state", length = 120)
    private String state;

    @Column(name = "address_postal_code", length = 40)
    private String postalCode;

    @Column(name = "address_country", length = 120)
    private String country;

    protected ContactAddress() {
    }

    public ContactAddress(String street, String city, String state, String postalCode, String country) {
        this.street = street;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }

    boolean isEmpty() {
        return street == null
                && city == null
                && state == null
                && postalCode == null
                && country == null;
    }
}
