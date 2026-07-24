package pojo;

import java.math.BigDecimal;

public class Product {

    private String title;
    private BigDecimal price;
    private String description;
    private String category;
    private Integer stockQuantity;

    public Product() {
    }

    public Product(String title, BigDecimal price, String description,
                   String category, Integer stockQuantity) {
        this.title = title;
        this.price = price;
        this.description = description;
        this.category = category;
        this.stockQuantity = stockQuantity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
}
