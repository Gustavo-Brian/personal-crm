CREATE TABLE contact_group_memberships (
    id BIGINT NOT NULL AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    contact_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_contact_group_memberships_group FOREIGN KEY (group_id) REFERENCES contact_groups (id) ON DELETE CASCADE,
    CONSTRAINT fk_contact_group_memberships_contact FOREIGN KEY (contact_id) REFERENCES contacts (id) ON DELETE CASCADE,
    CONSTRAINT uk_contact_group_memberships_group_contact UNIQUE (group_id, contact_id)
);

CREATE INDEX idx_contact_group_memberships_group_id ON contact_group_memberships (group_id);
CREATE INDEX idx_contact_group_memberships_contact_id ON contact_group_memberships (contact_id);
