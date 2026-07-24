package com.ecommerce.cart.dto;

import com.ecommerce.cart.model.Cart;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long cartId,
        Long userId,
        List<CartItemResponse> items,
        BigDecimal totalPrice
) {
    public static CartResponse from(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(CartItemResponse::from)
                .toList();
        // totalPrice = sum(price * quantity) - a small computed field the tests can verify.
        BigDecimal total = cart.getItems().stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(cart.getCartId(), cart.getUserId(), items, total);
    }
}
