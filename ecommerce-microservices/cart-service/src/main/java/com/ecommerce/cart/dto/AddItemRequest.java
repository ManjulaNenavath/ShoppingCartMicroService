package com.ecommerce.cart.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record AddItemRequest(

        @NotNull(message = "productId is required")
        Long productId,

        @NotNull(message = "quantity is required")
        @Positive(message = "quantity must be > 0")
        Integer quantity,

        @NotNull(message = "price is required")
        @PositiveOrZero(message = "price must be >= 0")
        BigDecimal price
) {
}
