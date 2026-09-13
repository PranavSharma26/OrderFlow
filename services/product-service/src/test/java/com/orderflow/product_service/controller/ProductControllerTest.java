package com.orderflow.product_service.controller;

import com.orderflow.product_service.entity.Product;
import com.orderflow.product_service.response.GlobalApiResponseHandler;
import com.orderflow.product_service.service.ProductService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalApiResponseHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;


    // ---------------------------------------------------------
    // GET ALL PRODUCTS
    // ---------------------------------------------------------

    @Test
    void getAllProducts_shouldReturn200() throws Exception {

        Product product = createProduct();

        when(productService.getAllProducts())
                .thenReturn(List.of(product));

        mockMvc.perform(
                        get("/api/products")
                )
                .andExpect(status().isOk());
    }


    // ---------------------------------------------------------
    // GET PRODUCT BY ID
    // ---------------------------------------------------------

    @Test
    void getProductById_shouldReturn200() throws Exception {

        Product product = createProduct();

        when(productService.getProductById(1L))
                .thenReturn(product);

        mockMvc.perform(
                        get("/api/products/1")
                )
                .andExpect(status().isOk());
    }


    // ---------------------------------------------------------
    // CREATE PRODUCT
    // ---------------------------------------------------------

    @Test
    void createProduct_shouldReturn201() throws Exception {

        Product product = createProduct();

        when(productService.createProduct(any(Product.class)))
                .thenReturn(product);

        String requestBody = """
                {
                    "name": "iPhone 17",
                    "description": "Apple smartphone",
                    "price": 79999.00,
                    "stockQuantity": 10
                }
                """;

        mockMvc.perform(
                        post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated());
    }


    // ---------------------------------------------------------
    // CREATE PRODUCT - VALIDATION
    // ---------------------------------------------------------

    @Test
    void createProduct_withInvalidData_shouldReturn400()
            throws Exception {

        String requestBody = """
                {
                    "name": "",
                    "description": "Invalid product",
                    "price": -10,
                    "stockQuantity": -5
                }
                """;

        mockMvc.perform(
                        post("/api/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }


    // ---------------------------------------------------------
    // UPDATE PRODUCT
    // ---------------------------------------------------------

    @Test
    void updateProduct_shouldReturn200() throws Exception {

        Product product = createProduct();

        when(productService.updateProduct(
                any(Long.class),
                any(Product.class)
        )).thenReturn(product);

        String requestBody = """
                {
                    "name": "iPhone 17 Pro",
                    "description": "Updated smartphone",
                    "price": 99999.00,
                    "stockQuantity": 20
                }
                """;

        mockMvc.perform(
                        put("/api/products/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk());
    }


    // ---------------------------------------------------------
    // UPDATE PRODUCT - VALIDATION
    // ---------------------------------------------------------

    @Test
    void updateProduct_withInvalidData_shouldReturn400()
            throws Exception {

        String requestBody = """
                {
                    "name": "",
                    "description": "Invalid product",
                    "price": -10,
                    "stockQuantity": -5
                }
                """;

        mockMvc.perform(
                        put("/api/products/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }


    // ---------------------------------------------------------
    // DELETE PRODUCT
    // ---------------------------------------------------------

    @Test
    void deleteProduct_shouldReturn204() throws Exception {

        mockMvc.perform(
                        delete("/api/products/1")
                )
                .andExpect(status().isNoContent());
    }


    // ---------------------------------------------------------
    // HELPER METHOD
    // ---------------------------------------------------------

    private Product createProduct() {

        Product product = new Product();

        product.setId(1L);
        product.setName("iPhone 17");
        product.setDescription("Apple smartphone");
        product.setPrice(new BigDecimal("79999.00"));
        product.setStockQuantity(10);

        return product;
    }
}