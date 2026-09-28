package com.komy.flatrentalapi.service;

import com.komy.flatrentalapi.entity.RefreshToken;
import com.komy.flatrentalapi.entity.User;
import com.komy.flatrentalapi.exception.InvalidCredentialsException;
import com.komy.flatrentalapi.exception.RefreshTokenReuseException;
import com.komy.flatrentalapi.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenRevocationService refreshTokenRevocationService;
    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 7;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, RefreshTokenRevocationService refreshTokenRevocationService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenRevocationService = refreshTokenRevocationService;
    }

    @Transactional
    public RefreshToken generateRefreshToken(User user) {
        var refreshToken = new RefreshToken();
        refreshToken.setToken(java.util.UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now().plus(REFRESH_TOKEN_VALIDITY_DAYS, java.time.temporal.ChronoUnit.DAYS));
        refreshToken.setRevoked(false);
        refreshToken.setUser(user);
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public RefreshToken rotateRefreshToken(String oldTokenValue) {
        var oldToken = refreshTokenRepository.findByToken(oldTokenValue)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

        if (oldToken.isRevoked()) {
            refreshTokenRevocationService.revokeAllUserTokens(oldToken.getUser());
            throw new RefreshTokenReuseException("Refresh token has been revoked");

        }
        if (oldToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidCredentialsException("Refresh token has expired");
        }
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        return generateRefreshToken(oldToken.getUser());
    }
}
