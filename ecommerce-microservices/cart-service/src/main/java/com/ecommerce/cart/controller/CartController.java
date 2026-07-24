package com.ecommerce.cart.controller;

import com.ecommerce.cart.dto.AddItemRequest;
import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cart endpoints.
 *   GET    /api/cart/{userId}                 -> 200 (auto-creates empty cart)
 *   POST   /api/cart/{userId}/items           -> 201 Created (item added, returns cart)
 *   DELETE /api/cart/{userId}/items/{itemId}  -> 204 (or 404 if item/cart missing)
 *   DELETE /api/cart/{userId}                 -> 204 (or 404 if cart missing)
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getCart(userId));
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<CartResponse> addItem(@PathVariable Long userId,
                                                @Valid @RequestBody AddItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addItem(userId, request));
    }

    @DeleteMapping("/{userId}/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long userId, @PathVariable Long itemId) {
        service.removeItem(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        service.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
