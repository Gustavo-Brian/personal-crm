package com.personalcrm.auth;

import com.personalcrm.user.User;

public record AuthenticatedUserResponse(
        Long id,
        String name,
        String email,
        String token,
        String tokenType
) {

    public static AuthenticatedUserResponse from(User user, String token) {
        return new AuthenticatedUserResponse(user.getId(), user.getName(), user.getEmail(), token, "Bearer");
    }
}
