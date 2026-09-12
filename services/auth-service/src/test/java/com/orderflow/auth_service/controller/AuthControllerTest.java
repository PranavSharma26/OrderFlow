package com.orderflow.auth_service.controller;

import com.orderflow.auth_service.dto.LoginRequest;
import com.orderflow.auth_service.dto.RegisterRequest;
import com.orderflow.auth_service.entity.User;
import com.orderflow.auth_service.enums.Role;
import com.orderflow.auth_service.repository.UserRepository;
import com.orderflow.auth_service.service.AuthService;

import com.orderflow.auth_service.service.JwtService;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.springframework.http.MediaType.APPLICATION_JSON;


@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    // ---------------------------------------------------------
    // REGISTER
    // ---------------------------------------------------------

    @Test
    void register_shouldReturnSuccessResponse() throws Exception {

        doNothing()
                .when(authService)
                .register(any(RegisterRequest.class));

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                {
                                    "firstName": "Pranav",
                                    "lastName": "Sharma",
                                    "email": "pranav@example.com",
                                    "password": "password123",
                                    "role": "CUSTOMER"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("User registered successfully"))
                .andExpect(jsonPath("$.status")
                        .value(200))
                .andExpect(jsonPath("$.success")
                        .value(true));

        verify(authService)
                .register(any(RegisterRequest.class));
    }


    // ---------------------------------------------------------
    // LOGIN
    // ---------------------------------------------------------

    @Test
    void login_shouldReturnJwtToken() throws Exception {

        when(authService.login(any(LoginRequest.class)))
                .thenReturn("mock-jwt-token");

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                {
                                    "email": "pranav@example.com",
                                    "password": "password123"
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("User logged in successfully"))
                .andExpect(jsonPath("$.status")
                        .value(200))
                .andExpect(jsonPath("$.success")
                        .value(true))
                .andExpect(jsonPath("$.data")
                        .value("mock-jwt-token"));

        verify(authService)
                .login(any(LoginRequest.class));
    }


    // ---------------------------------------------------------
    // ME
    // ---------------------------------------------------------

    @Test
    void me_shouldReturnUserEmail() throws Exception {

        User user = new User(
                "Pranav",
                "Sharma",
                "pranav@example.com",
                "hashed-password",
                Role.CUSTOMER
        );

        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal())
                .thenReturn(user);

        mockMvc.perform(
                        get("/api/v1/auth/me")
                                .principal(authentication)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("User fetched successfully"))
                .andExpect(jsonPath("$.status")
                        .value(200))
                .andExpect(jsonPath("$.success")
                        .value(true))
                .andExpect(jsonPath("$.data")
                        .value("pranav@example.com"));
    }
}