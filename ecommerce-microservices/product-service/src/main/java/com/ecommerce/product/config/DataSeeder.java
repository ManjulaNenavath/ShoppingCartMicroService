package com.ecommerce.product.config;

import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Seeds a few products at startup so GET /api/products returns data out of the box.
 * Handy when demoing and when writing your first "list is not empty" assertion.
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedProducts(ProductRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            repository.save(product("Wireless Mouse", "59.99", "Ergonomic 2.4GHz mouse", "electronics", 120));
            repository.save(product("Mechanical Keyboard", "129.00", "Hot-swap RGB keyboard", "electronics", 45));
            repository.save(product("Coffee Mug", "12.50", "350ml ceramic mug", "kitchen", 300));
        };
    }

    private Product product(String title, String price, String desc, String category, int stock) {
        Product p = new Product();
        p.setTitle(title);
        p.setPrice(new BigDecimal(price));
        p.setDescription(desc);
        p.setCategory(category);
        p.setStockQuantity(stock);
        return p;
    }
}
