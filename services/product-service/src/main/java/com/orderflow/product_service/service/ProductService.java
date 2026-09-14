package com.orderflow.product_service.service;

import com.orderflow.product_service.dto.StockItemRequest;
import com.orderflow.product_service.entity.Product;
import com.orderflow.product_service.exception.ProductNotFoundException;
import com.orderflow.product_service.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Create product
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    // Get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Get product by ID
    public Product getProductById(Long id) {

        return productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + id
                        )
                );
    }

    // Update product
    public Product updateProduct(
            Long id,
            Product updatedProduct
    ) {

        Product existingProduct = getProductById(id);

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setStockQuantity(
                updatedProduct.getStockQuantity()
        );

        return productRepository.save(existingProduct);
    }

    // Delete product
    public void deleteProduct(Long id) {

        Product existingProduct = getProductById(id);

        productRepository.delete(existingProduct);
    }

    // Decrease stock for all order items atomically
    @Transactional
    public boolean decreaseStock(
            List<StockItemRequest> items
    ) {

        for (StockItemRequest item : items) {

            int updatedRows = productRepository.decreaseStock(
                    item.getProductId(),
                    item.getQuantity()
            );

            if (updatedRows == 0) {
                throw new IllegalStateException(
                        "Insufficient stock for product: "
                                + item.getProductId()
                );
            }
        }

        return true;
    }
}