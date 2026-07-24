package com.ecommerce.user.dto;

import com.ecommerce.user.model.User;

/**
 * Outgoing representation of a user. Note there is NO password field here -
 * that is the whole point of separating DTO from entity.
 */
public record UserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String phone
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone()
        );
    }
}
