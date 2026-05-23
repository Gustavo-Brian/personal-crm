package com.personalcrm.contact;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ImportantDateResponse(
        Long id,
        Long contactId,
        String title,
        LocalDate date,
        ImportantDateType type,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    static ImportantDateResponse from(ImportantDate importantDate) {
        return new ImportantDateResponse(
                importantDate.getId(),
                importantDate.getContact().getId(),
                importantDate.getTitle(),
                importantDate.getDate(),
                importantDate.getType(),
                importantDate.getDescription(),
                importantDate.getCreatedAt(),
                importantDate.getUpdatedAt()
        );
    }
}
