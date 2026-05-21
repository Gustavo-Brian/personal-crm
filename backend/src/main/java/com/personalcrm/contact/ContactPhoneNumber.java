package com.personalcrm.contact;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ContactPhoneNumber {

    @Column(length = 80)
    private String label;

    @Column(name = "phone_number", nullable = false, length = 40)
    private String number;

    protected ContactPhoneNumber() {
    }

    public ContactPhoneNumber(String label, String number) {
        this.label = label;
        this.number = number;
    }

    public String getLabel() {
        return label;
    }

    public String getNumber() {
        return number;
    }
}
