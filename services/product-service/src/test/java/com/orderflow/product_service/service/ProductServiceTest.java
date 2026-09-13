package com.orderflow.product_service.service;

import com.orderflow.product_service.entity.Product;
import com.orderflow.product_service.exception.ProductNotFoundException;
import com.orderflow.product_service.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {

        product = new Product();

        product.setId(1L);
        product.setName("iPhone 17");
        product.setDescription("Apple smartphone");
        product.setPrice(new BigDecimal("79999.00"));
        product.setStockQuantity(10);
    }


    // ---------------------------------------------------------
    // CREATE PRODUCT
    // ---------------------------------------------------------

    @Test
    void createProduct_shouldSaveProduct() {

        when(productRepository.save(product))
                .thenReturn(product);

        Product result = productService.createProduct(product);

        assertEquals(product, result);

        verify(productRepository).save(product);
    }


    // ---------------------------------------------------------
    // GET ALL PRODUCTS
    // ---------------------------------------------------------

    @Test
    void getAllProducts_shouldReturnAllProducts() {

        when(productRepository.findAll())
                .thenReturn(List.of(product));

        List<Product> result =
                productService.getAllProducts();

        assertEquals(1, result.size());
        assertEquals(product, result.get(0));

        verify(productRepository).findAll();
    }


    // ---------------------------------------------------------
    // GET PRODUCT BY ID
    // ---------------------------------------------------------

    @Test
    void getProductById_shouldReturnProduct() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        Product result =
                productService.getProductById(1L);

        assertEquals(product, result);

        verify(productRepository).findById(1L);
    }


    // ---------------------------------------------------------
    // GET PRODUCT BY ID - NOT FOUND
    // ---------------------------------------------------------

    @Test
    void getProductById_whenProductDoesNotExist_shouldThrowException() {

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductById(99L)
        );

        verify(productRepository).findById(99L);
    }


    // ---------------------------------------------------------
    // UPDATE PRODUCT
    // ---------------------------------------------------------

    @Test
    void updateProduct_shouldUpdateAndSaveProduct() {

        Product updatedProduct = new Product();

        updatedProduct.setName("iPhone 17 Pro");
        updatedProduct.setDescription("Updated smartphone");
        updatedProduct.setPrice(new BigDecimal("99999.00"));
        updatedProduct.setStockQuantity(20);

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(productRepository.save(any(Product.class)))
                .thenReturn(product);

        Product result =
                productService.updateProduct(
                        1L,
                        updatedProduct
                );

        assertEquals("iPhone 17 Pro", result.getName());
        assertEquals(
                "Updated smartphone",
                result.getDescription()
        );
        assertEquals(
                new BigDecimal("99999.00"),
                result.getPrice()
        );
        assertEquals(20, result.getStockQuantity());

        verify(productRepository).findById(1L);
        verify(productRepository).save(product);
    }


    // ---------------------------------------------------------
    // UPDATE PRODUCT - NOT FOUND
    // ---------------------------------------------------------

    @Test
    void updateProduct_whenProductDoesNotExist_shouldThrowException() {

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        Product updatedProduct = new Product();

        updatedProduct.setName("New Product");
        updatedProduct.setPrice(new BigDecimal("100.00"));
        updatedProduct.setStockQuantity(5);

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.updateProduct(
                        99L,
                        updatedProduct
                )
        );

        verify(productRepository).findById(99L);
    }


    // ---------------------------------------------------------
    // DELETE PRODUCT
    // ---------------------------------------------------------

    @Test
    void deleteProduct_shouldDeleteProduct() {

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        doNothing()
                .when(productRepository)
                .delete(product);

        productService.deleteProduct(1L);

        verify(productRepository).findById(1L);
        verify(productRepository).delete(product);
    }


    // ---------------------------------------------------------
    // DELETE PRODUCT - NOT FOUND
    // ---------------------------------------------------------

    @Test
    void deleteProduct_whenProductDoesNotExist_shouldThrowException() {

        when(productRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.deleteProduct(99L)
        );

        verify(productRepository).findById(99L);
    }
}