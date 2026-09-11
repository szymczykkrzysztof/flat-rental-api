package com.komy.flatrentalapi.service;

import com.komy.flatrentalapi.dto.auth.*;
import com.komy.flatrentalapi.entity.User;
import com.komy.flatrentalapi.entity.enums.Role;
import com.komy.flatrentalapi.exception.EmailConflictException;
import com.komy.flatrentalapi.exception.InvalidCredentialsException;
import com.komy.flatrentalapi.repository.RefreshTokenRepository;
import com.komy.flatrentalapi.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;

    public AuthService(UserRepository userRepository, JwtService jwtService, PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenService = refreshTokenService;
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

        var accessToken = jwtService.generateToken(user.getEmail());
        var refreshToken = refreshTokenService.generateRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken.getToken());
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        var newRefreshToken = refreshTokenService.rotateRefreshToken(request.refreshToken());
        var newAccessToken = jwtService.generateToken(newRefreshToken.getUser().getEmail());
        return new AuthResponse(newAccessToken, newRefreshToken.getToken());
    }

    public void logout(RefreshTokenRequest request) {
        var token = refreshTokenRepository.findByToken(request.refreshToken());
        token.ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }
}
