package com.komy.flatrentalapi.service;

import com.komy.flatrentalapi.entity.RefreshToken;
import com.komy.flatrentalapi.entity.User;
import com.komy.flatrentalapi.exception.InvalidCredentialsException;
import com.komy.flatrentalapi.exception.RefreshTokenReuseException;
import com.komy.flatrentalapi.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private static final long REFRESH_TOKEN_VALIDITY_DAYS = 7;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public RefreshToken generateRefreshToken(User user) {
        var refreshToken = new RefreshToken();
        refreshToken.setToken(java.util.UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now().plus(REFRESH_TOKEN_VALIDITY_DAYS, java.time.temporal.ChronoUnit.DAYS));
        refreshToken.setRevoked(false);
        refreshToken.setUser(user);
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken rotateRefreshToken(String oldTokenValue) {
        var oldToken = refreshTokenRepository.findByToken(oldTokenValue)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

        if (oldToken.isRevoked()) {
            revokeAllUserTokens(oldToken.getUser());
            throw new RefreshTokenReuseException("Refresh token has been revoked");

        }
        if (oldToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidCredentialsException("Refresh token has expired");
        }
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        return generateRefreshToken(oldToken.getUser());
    }

    public void revokeAllUserTokens(User user) {
        var tokens = refreshTokenRepository.findByUserIdAndRevokedFalse(user.getId());
        tokens.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);
    }
}
