package com.ecommerce.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank(message = "title is required")
        String title,

        @NotNull(message = "price is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "price must be >= 0")
        BigDecimal price,

        String description,
        String category,

        @NotNull(message = "stockQuantity is required")
        @PositiveOrZero(message = "stockQuantity must be >= 0")
        Integer stockQuantity
) {
}
