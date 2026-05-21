ALTER TABLE contacts ADD address_street VARCHAR(255) NULL;
ALTER TABLE contacts ADD address_city VARCHAR(120) NULL;
ALTER TABLE contacts ADD address_state VARCHAR(120) NULL;
ALTER TABLE contacts ADD address_postal_code VARCHAR(40) NULL;
ALTER TABLE contacts ADD address_country VARCHAR(120) NULL;
ALTER TABLE contacts ADD notes VARCHAR(2000) NULL;

CREATE TABLE contact_phone_numbers (
    contact_id BIGINT NOT NULL,
    sort_order INT NOT NULL,
    label VARCHAR(80) NULL,
    phone_number VARCHAR(40) NOT NULL,
    CONSTRAINT fk_contact_phone_numbers_contact FOREIGN KEY (contact_id) REFERENCES contacts (id) ON DELETE CASCADE
);

CREATE INDEX idx_contact_phone_numbers_contact_id ON contact_phone_numbers (contact_id);

CREATE TABLE contact_email_addresses (
    contact_id BIGINT NOT NULL,
    sort_order INT NOT NULL,
    label VARCHAR(80) NULL,
    email_address VARCHAR(255) NOT NULL,
    CONSTRAINT fk_contact_email_addresses_contact FOREIGN KEY (contact_id) REFERENCES contacts (id) ON DELETE CASCADE
);

CREATE INDEX idx_contact_email_addresses_contact_id ON contact_email_addresses (contact_id);
