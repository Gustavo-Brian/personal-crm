package com.personalcrm.contact;

import com.personalcrm.user.User;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 160)
    private String organization;

    @Column(name = "job_title", length = 120)
    private String jobTitle;

    private LocalDate birthday;

    @Embedded
    private ContactAddress address;

    @Column(length = 2000)
    private String notes;

    @ElementCollection
    @CollectionTable(name = "contact_phone_numbers", joinColumns = @JoinColumn(name = "contact_id"))
    @OrderColumn(name = "sort_order")
    private List<ContactPhoneNumber> phoneNumbers = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "contact_email_addresses", joinColumns = @JoinColumn(name = "contact_id"))
    @OrderColumn(name = "sort_order")
    private List<ContactEmailAddress> emailAddresses = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Contact() {
    }

    public Contact(User owner, String name, String organization, String jobTitle, LocalDate birthday) {
        this(owner, name, organization, jobTitle, birthday, null, null, List.of(), List.of());
    }

    public Contact(
            User owner,
            String name,
            String organization,
            String jobTitle,
            LocalDate birthday,
            ContactAddress address,
            String notes,
            List<ContactPhoneNumber> phoneNumbers,
            List<ContactEmailAddress> emailAddresses
    ) {
        this.owner = owner;
        this.name = name;
        this.organization = organization;
        this.jobTitle = jobTitle;
        this.birthday = birthday;
        this.address = normalizeAddress(address);
        this.notes = notes;
        replacePhoneNumbers(phoneNumbers);
        replaceEmailAddresses(emailAddresses);
    }

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getOrganization() {
        return organization;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public ContactAddress getAddress() {
        return address;
    }

    public String getNotes() {
        return notes;
    }

    public List<ContactPhoneNumber> getPhoneNumbers() {
        return List.copyOf(phoneNumbers);
    }

    public List<ContactEmailAddress> getEmailAddresses() {
        return List.copyOf(emailAddresses);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void updateBasicInfo(String name, String organization, String jobTitle, LocalDate birthday) {
        this.name = name;
        this.organization = organization;
        this.jobTitle = jobTitle;
        this.birthday = birthday;
    }

    public void updateDetails(
            ContactAddress address,
            String notes,
            List<ContactPhoneNumber> phoneNumbers,
            List<ContactEmailAddress> emailAddresses
    ) {
        this.address = normalizeAddress(address);
        this.notes = notes;
        replacePhoneNumbers(phoneNumbers);
        replaceEmailAddresses(emailAddresses);
    }

    private ContactAddress normalizeAddress(ContactAddress address) {
        if (address == null || address.isEmpty()) {
            return null;
        }
        return address;
    }

    private void replacePhoneNumbers(List<ContactPhoneNumber> phoneNumbers) {
        this.phoneNumbers.clear();
        if (phoneNumbers != null) {
            this.phoneNumbers.addAll(phoneNumbers);
        }
    }

    private void replaceEmailAddresses(List<ContactEmailAddress> emailAddresses) {
        this.emailAddresses.clear();
        if (emailAddresses != null) {
            this.emailAddresses.addAll(emailAddresses);
        }
    }
}
