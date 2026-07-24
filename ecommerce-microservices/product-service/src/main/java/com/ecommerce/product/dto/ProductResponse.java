package com.ecommerce.product.dto;

import com.ecommerce.product.model.Product;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String title,
        BigDecimal price,
        String description,
        String category,
        Integer stockQuantity
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(), p.getTitle(), p.getPrice(),
                p.getDescription(), p.getCategory(), p.getStockQuantity());
    }
}
