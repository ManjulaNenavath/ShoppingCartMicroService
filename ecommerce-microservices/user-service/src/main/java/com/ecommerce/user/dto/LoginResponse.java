package com.ecommerce.user.dto;

public record LoginResponse(
        String token,
        Long userId,
        String username,
        String message
) {
}
