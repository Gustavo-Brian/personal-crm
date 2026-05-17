package com.personalcrm.auth;

import com.personalcrm.user.User;

public record AuthenticatedUserResponse(
        Long id,
        String name,
        String email
) {

    public static AuthenticatedUserResponse from(User user) {
        return new AuthenticatedUserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
