CREATE TABLE contact_academic_formations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    contact_id BIGINT NOT NULL,
    institution VARCHAR(160) NOT NULL,
    degree VARCHAR(120) NULL,
    field_of_study VARCHAR(160) NULL,
    start_date DATE NULL,
    end_date DATE NULL,
    description VARCHAR(1000) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_contact_academic_formations_contact FOREIGN KEY (contact_id) REFERENCES contacts (id) ON DELETE CASCADE
);

CREATE INDEX idx_contact_academic_formations_contact_id ON contact_academic_formations (contact_id);
