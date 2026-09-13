package com.orderflow.product_service.config;

import com.orderflow.product_service.response.ApiResponse;
import com.orderflow.product_service.security.JwtAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // SELLER - Create product
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/products"
                        ).hasRole("SELLER")

                        // SELLER - Update product
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/products/**"
                        ).hasRole("SELLER")

                        // SELLER - Delete product
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/products/**"
                        ).hasRole("SELLER")

                        // CUSTOMER + SELLER - View products
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/products",
                                "/api/products/**"
                        ).hasAnyRole(
                                "CUSTOMER",
                                "SELLER"
                        )

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                .exceptionHandling(exception ->
                        exception

                                // 401 - Not authenticated
                                .authenticationEntryPoint(
                                        (request, response, authException) -> {

                                            response.setStatus(401);
                                            response.setContentType(
                                                    "application/json"
                                            );

                                            ApiResponse apiResponse =
                                                    new ApiResponse(
                                                            LocalDateTime.now(),
                                                            "Authentication required",
                                                            401,
                                                            false
                                                    );

                                            ObjectMapper objectMapper =
                                                    new ObjectMapper();

                                            response.getWriter().write(
                                                    objectMapper.writeValueAsString(
                                                            apiResponse
                                                    )
                                            );
                                        }
                                )

                                // 403 - Not authorized
                                .accessDeniedHandler(
                                        (request, response,
                                         accessDeniedException) -> {

                                            response.setStatus(403);
                                            response.setContentType(
                                                    "application/json"
                                            );

                                            ApiResponse apiResponse =
                                                    new ApiResponse(
                                                            LocalDateTime.now(),
                                                            "Access denied",
                                                            403,
                                                            false
                                                    );

                                            ObjectMapper objectMapper =
                                                    new ObjectMapper();

                                            response.getWriter().write(
                                                    objectMapper.writeValueAsString(
                                                            apiResponse
                                                    )
                                            );
                                        }
                                )
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}