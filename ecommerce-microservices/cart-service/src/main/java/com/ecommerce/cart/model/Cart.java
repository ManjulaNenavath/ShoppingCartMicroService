package com.ecommerce.cart.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * A user's shopping cart. One cart per userId.
 *
 * The @OneToMany with cascade=ALL + orphanRemoval means: saving the cart saves
 * its items, and removing an item from the list deletes its row. That is exactly
 * the behaviour we want for "remove item" and "clear cart".
 */
@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;

    private Long userId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    public Cart() {
    }

    public Cart(Long userId) {
        this.userId = userId;
    }

    public void addItem(CartItem item) {
        item.setCart(this);
        this.items.add(item);
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }
}
