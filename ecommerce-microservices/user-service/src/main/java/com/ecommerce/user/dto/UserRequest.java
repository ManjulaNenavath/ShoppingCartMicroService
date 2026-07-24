package com.ecommerce.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Incoming payload for register (POST) and profile update (PUT).
 *
 * Java records are immutable data carriers - perfect for DTOs.
 * The jakarta.validation annotations are enforced by @Valid in the controller,
 * and a violation is automatically turned into a 400 Bad Request by our
 * GlobalExceptionHandler.
 */
public record UserRequest(

        @NotBlank(message = "username is required")
        @Size(min = 3, max = 30, message = "username must be 3-30 characters")
        String username,

        @NotBlank(message = "password is required")
        @Size(min = 6, message = "password must be at least 6 characters")
        String password,

        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        String email,

        String firstName,
        String lastName,
        String phone
) {
}
