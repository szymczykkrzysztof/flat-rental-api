package com.komy.flatrentalapi.service;

import com.komy.flatrentalapi.dto.auth.AuthResponse;
import com.komy.flatrentalapi.dto.auth.LoginRequest;
import com.komy.flatrentalapi.dto.auth.RegisterRequest;
import com.komy.flatrentalapi.dto.auth.UserResponse;
import com.komy.flatrentalapi.entity.User;
import com.komy.flatrentalapi.entity.enums.Role;
import com.komy.flatrentalapi.exception.EmailConflictException;
import com.komy.flatrentalapi.exception.InvalidCredentialsException;
import com.komy.flatrentalapi.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailConflictException("User already exists");
        }
        var hashedPassword = passwordEncoder.encode(request.password());
        var user = new User();
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPasswordHash(hashedPassword);
        user.setRole(Role.TENANT);
        userRepository.save(user);
        return new UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        var token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token);
    }
}
