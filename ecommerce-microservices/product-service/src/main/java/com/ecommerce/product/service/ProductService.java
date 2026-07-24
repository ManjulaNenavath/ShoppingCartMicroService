package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<ProductResponse> getAll() {
        return repository.findAll().stream().map(ProductResponse::from).toList();
    }

    public ProductResponse getById(Long id) {
        return repository.findById(id)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("product not found with id: " + id));
    }

    public ProductResponse create(ProductRequest request) {
        Product product = apply(new Product(), request);
        return ProductResponse.from(repository.save(product));
    }

    public ProductResponse update(Long id, ProductRequest request) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("product not found with id: " + id));
        return ProductResponse.from(repository.save(apply(product, request)));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("product not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private Product apply(Product product, ProductRequest request) {
        product.setTitle(request.title());
        product.setPrice(request.price());
        product.setDescription(request.description());
        product.setCategory(request.category());
        product.setStockQuantity(request.stockQuantity());
        return product;
    }
}
