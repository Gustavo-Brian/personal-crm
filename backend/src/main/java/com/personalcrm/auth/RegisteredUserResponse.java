package com.personalcrm.auth;

import com.personalcrm.user.User;

public record RegisteredUserResponse(
        Long id,
        String name,
        String email
) {

    public static RegisteredUserResponse from(User user) {
        return new RegisteredUserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
