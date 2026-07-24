package com.ecommerce.cart.dto;

import com.ecommerce.cart.model.CartItem;

import java.math.BigDecimal;

public record CartItemResponse(
        Long itemId,
        Long productId,
        Integer quantity,
        BigDecimal price
) {
    public static CartItemResponse from(CartItem item) {
        return new CartItemResponse(item.getItemId(), item.getProductId(),
                item.getQuantity(), item.getPrice());
    }
}
