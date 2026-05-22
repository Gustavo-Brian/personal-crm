package com.personalcrm.group;

import com.personalcrm.contact.Contact;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "contact_group_memberships",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_contact_group_memberships_group_contact",
                        columnNames = {"group_id", "contact_id"}
                )
        }
)
public class ContactGroupMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private ContactGroup group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ContactGroupMembership() {
    }

    public ContactGroupMembership(ContactGroup group, Contact contact) {
        this.group = group;
        this.contact = contact;
    }

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public ContactGroup getGroup() {
        return group;
    }

    public Contact getContact() {
        return contact;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
