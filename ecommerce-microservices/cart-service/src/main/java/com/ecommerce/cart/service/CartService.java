package com.ecommerce.cart.service;

import com.ecommerce.cart.dto.AddItemRequest;
import com.ecommerce.cart.dto.CartResponse;
import com.ecommerce.cart.exception.ResourceNotFoundException;
import com.ecommerce.cart.model.Cart;
import com.ecommerce.cart.model.CartItem;
import com.ecommerce.cart.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartService {

    private final CartRepository repository;

    public CartService(CartRepository repository) {
        this.repository = repository;
    }

    /** GET cart: auto-creates an empty cart for a new user so callers always get a cart. */
    public CartResponse getCart(Long userId) {
        Cart cart = repository.findByUserId(userId).orElseGet(() -> repository.save(new Cart(userId)));
        return CartResponse.from(cart);
    }

    /** Add an item; creates the cart on first add. */
    public CartResponse addItem(Long userId, AddItemRequest request) {
        Cart cart = repository.findByUserId(userId).orElseGet(() -> repository.save(new Cart(userId)));
        cart.addItem(new CartItem(request.productId(), request.quantity(), request.price()));
        return CartResponse.from(repository.save(cart));
    }

    public void removeItem(Long userId, Long itemId) {
        Cart cart = repository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("cart not found for userId: " + userId));
        boolean removed = cart.getItems().removeIf(i -> i.getItemId().equals(itemId));
        if (!removed) {
            throw new ResourceNotFoundException("item not found with id: " + itemId);
        }
        repository.save(cart); // orphanRemoval deletes the detached item row
    }

    public void clearCart(Long userId) {
        Cart cart = repository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("cart not found for userId: " + userId));
        cart.getItems().clear();
        repository.save(cart);
    }
}
