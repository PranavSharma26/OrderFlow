package com.orderflow.product_service.security;

import com.orderflow.product_service.config.SecurityConfig;
import com.orderflow.product_service.controller.ProductController;
import com.orderflow.product_service.entity.Product;
import com.orderflow.product_service.response.GlobalApiResponseHandler;
import com.orderflow.product_service.service.ProductService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "jwt.secret=VGhpc0lzQVNlY3VyZVRlc3RTZWNyZXRLZXlGb3JPcmRlckZsb3c="
})
class ProductSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    private String customerToken;
    private String sellerToken;

    private static final String TEST_SECRET =
            "ThisIsASecureTestSecretKeyForOrderFlow";

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(
                TEST_SECRET.getBytes(StandardCharsets.UTF_8)
        );
    }

    @BeforeEach
    void setUp() {

        customerToken = createToken(
                "customer@orderflow.com",
                "CUSTOMER"
        );

        sellerToken = createToken(
                "seller@orderflow.com",
                "SELLER"
        );

        Product product = new Product();

        product.setId(1L);
        product.setName("iPhone 17");
        product.setDescription("Apple smartphone");
        product.setPrice(new BigDecimal("79999.00"));
        product.setStockQuantity(10);

        when(productService.getAllProducts())
                .thenReturn(List.of(product));

        when(productService.getProductById(1L))
                .thenReturn(product);
    }

    private String createToken(
            String email,
            String role
    ) {

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + 3600000
                        )
                )
                .signWith(getSigningKey())
                .compact();
    }

    // ==========================================
    // NO JWT
    // ==========================================

    @Test
    void requestWithoutToken_shouldReturn401()
            throws Exception {

        mockMvc.perform(
                get("/api/products")
        ).andExpect(status().isUnauthorized());
    }

    // ==========================================
    // CUSTOMER
    // ==========================================

    @Test
    void customer_shouldBeAbleToGetProducts()
            throws Exception {

        mockMvc.perform(
                get("/api/products")
                        .header(
                                "Authorization",
                                "Bearer " + customerToken
                        )
        ).andExpect(status().isOk());
    }

    @Test
    void customer_shouldBeAbleToGetProductById()
            throws Exception {

        mockMvc.perform(
                get("/api/products/1")
                        .header(
                                "Authorization",
                                "Bearer " + customerToken
                        )
        ).andExpect(status().isOk());
    }

    @Test
    void customer_shouldNotBeAbleToCreateProduct()
            throws Exception {

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
                        .header(
                                "Authorization",
                                "Bearer " + customerToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        ).andExpect(status().isForbidden());
    }

    @Test
    void customer_shouldNotBeAbleToUpdateProduct()
            throws Exception {

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
                        .header(
                                "Authorization",
                                "Bearer " + customerToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        ).andExpect(status().isForbidden());
    }

    @Test
    void customer_shouldNotBeAbleToDeleteProduct()
            throws Exception {

        mockMvc.perform(
                delete("/api/products/1")
                        .header(
                                "Authorization",
                                "Bearer " + customerToken
                        )
        ).andExpect(status().isForbidden());
    }

    // ==========================================
    // SELLER
    // ==========================================

    @Test
    void seller_shouldBeAbleToGetProducts()
            throws Exception {

        mockMvc.perform(
                get("/api/products")
                        .header(
                                "Authorization",
                                "Bearer " + sellerToken
                        )
        ).andExpect(status().isOk());
    }

    @Test
    void seller_shouldBeAbleToCreateProduct()
            throws Exception {

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
                        .header(
                                "Authorization",
                                "Bearer " + sellerToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        ).andExpect(status().isCreated());
    }

    @Test
    void seller_shouldBeAbleToUpdateProduct()
            throws Exception {

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
                        .header(
                                "Authorization",
                                "Bearer " + sellerToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        ).andExpect(status().isOk());
    }

    @Test
    void seller_shouldBeAbleToDeleteProduct()
            throws Exception {

        mockMvc.perform(
                delete("/api/products/1")
                        .header(
                                "Authorization",
                                "Bearer " + sellerToken
                        )
        ).andExpect(status().isNoContent());
    }
}