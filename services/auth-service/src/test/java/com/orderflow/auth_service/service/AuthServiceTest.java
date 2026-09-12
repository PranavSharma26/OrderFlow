package com.orderflow.auth_service.service;

import com.orderflow.auth_service.dto.LoginRequest;
import com.orderflow.auth_service.dto.RegisterRequest;
import com.orderflow.auth_service.entity.User;
import com.orderflow.auth_service.enums.Role;
import com.orderflow.auth_service.exception.ApiException;
import com.orderflow.auth_service.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;


    // =========================================================
    // REGISTER TESTS
    // =========================================================

    @Test
    void register_shouldSaveUserSuccessfully() {

        RegisterRequest request = new RegisterRequest(
                "Pranav",
                "Sharma",
                "pranav@example.com",
                "password123",
                Role.CUSTOMER
        );

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        authService.register(request);

        verify(userRepository).save(any(User.class));
    }


    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {

        RegisterRequest request = new RegisterRequest(
                "Pranav",
                "Sharma",
                "pranav@example.com",
                "password123",
                Role.CUSTOMER
        );

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(true);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> authService.register(request)
        );

        assertEquals(
                "User with this email already exists",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void register_shouldThrowException_whenRoleIsAdmin() {

        RegisterRequest request = new RegisterRequest(
                "Pranav",
                "Sharma",
                "pranav@example.com",
                "password123",
                Role.ADMIN
        );

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> authService.register(request)
        );

        assertEquals(
                "You cannot register as an admin",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void register_shouldEncodePasswordBeforeSaving() {

        RegisterRequest request = new RegisterRequest(
                "Pranav",
                "Sharma",
                "pranav@example.com",
                "password123",
                Role.CUSTOMER
        );

        when(userRepository.existsByEmail(request.getEmail()))
                .thenReturn(false);

        authService.register(request);

        verify(userRepository).save(argThat(user ->
                user.getPassword() != null
                        && !user.getPassword().equals("password123")
        ));
    }


    // =========================================================
    // LOGIN TESTS
    // =========================================================

    @Test
    void login_shouldReturnJwtToken_whenCredentialsAreCorrect() {

        LoginRequest request = new LoginRequest();
        request.setEmail("pranav@example.com");
        request.setPassword("password123");

        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
                passwordEncoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

        User user = new User(
                "Pranav",
                "Sharma",
                "pranav@example.com",
                passwordEncoder.encode("password123"),
                Role.CUSTOMER
        );

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        when(jwtService.generateToken(user))
                .thenReturn("mock-jwt-token");

        String token = authService.login(request);

        assertEquals("mock-jwt-token", token);

        verify(userRepository).findByEmail(request.getEmail());
        verify(jwtService).generateToken(user);
    }


    @Test
    void login_shouldThrowException_whenUserDoesNotExist() {

        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        ApiException exception = assertThrows(
                ApiException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "User with this email don't exists",
                exception.getMessage()
        );

        verify(jwtService, never()).generateToken(any(User.class));
    }


    @Test
    void login_shouldThrowException_whenPasswordIsIncorrect() {

        LoginRequest request = new LoginRequest();
        request.setEmail("pranav@example.com");
        request.setPassword("wrongpassword");

        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
                passwordEncoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

        User user = new User(
                "Pranav",
                "Sharma",
                "pranav@example.com",
                passwordEncoder.encode("password123"),
                Role.CUSTOMER
        );

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.of(user));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "Incorrect Password",
                exception.getMessage()
        );

        verify(jwtService, never()).generateToken(any(User.class));
    }
}