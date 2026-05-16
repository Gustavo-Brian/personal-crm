package com.personalcrm.common;

import java.util.Map;

public record ApiError(
        String message,
        Map<String, String> errors
) {

    public static ApiError of(String message) {
        return new ApiError(message, Map.of());
    }

    public static ApiError validation(Map<String, String> errors) {
        return new ApiError("Validation failed", errors);
    }
}
