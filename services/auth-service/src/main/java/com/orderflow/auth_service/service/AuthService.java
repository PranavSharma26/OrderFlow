package com.orderflow.auth_service.service;

import com.orderflow.auth_service.dto.RegisterRequest;
import com.orderflow.auth_service.entity.User;
import com.orderflow.auth_service.enums.Role;
import com.orderflow.auth_service.exception.ApiException;
import com.orderflow.auth_service.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void register(RegisterRequest request) {
        // Check if user exists
        if(userRepository.existsByEmail(request.getEmail())){
            throw new ApiException(400, "User with this email already exists");
        }
        if(request.getRole() == Role.ADMIN){
            throw new ApiException(400, "You cannot register as an admin");
        }

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(hashedPassword);
        user.setRole(request.getRole());
        userRepository.save(user);
    }

}
