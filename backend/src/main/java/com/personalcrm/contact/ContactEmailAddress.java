package com.personalcrm.contact;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ContactEmailAddress {

    @Column(length = 80)
    private String label;

    @Column(name = "email_address", nullable = false, length = 255)
    private String email;

    protected ContactEmailAddress() {
    }

    public ContactEmailAddress(String label, String email) {
        this.label = label;
        this.email = email;
    }

    public String getLabel() {
        return label;
    }

    public String getEmail() {
        return email;
    }
}
