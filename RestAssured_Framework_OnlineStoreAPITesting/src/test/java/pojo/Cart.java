package pojo;

import java.math.BigDecimal;

/**
 * Represents a single "add item to cart" payload (productId, quantity, price).
 * Kept flat to match the POST /api/cart/{userId}/items body.
 */
public class Cart {

    private Long productId;
    private Integer quantity;
    private BigDecimal price;

    public Cart() {
    }

    public Cart(Long productId, Integer quantity, BigDecimal price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
