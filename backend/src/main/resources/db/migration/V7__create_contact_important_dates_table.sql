CREATE TABLE contact_important_dates (
    id BIGINT NOT NULL AUTO_INCREMENT,
    contact_id BIGINT NOT NULL,
    title VARCHAR(160) NOT NULL,
    important_date DATE NOT NULL,
    date_type VARCHAR(40) NOT NULL,
    description VARCHAR(1000) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_contact_important_dates_contact FOREIGN KEY (contact_id) REFERENCES contacts (id) ON DELETE CASCADE
);

CREATE INDEX idx_contact_important_dates_contact_id ON contact_important_dates (contact_id);
CREATE INDEX idx_contact_important_dates_contact_date ON contact_important_dates (contact_id, important_date);
