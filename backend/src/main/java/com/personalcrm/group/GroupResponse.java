package com.personalcrm.group;

import java.time.LocalDateTime;

public record GroupResponse(
        Long id,
        String name,
        String description,
        String colorHex,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static GroupResponse from(ContactGroup group) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getColorHex(),
                group.getCreatedAt(),
                group.getUpdatedAt()
        );
    }
}
