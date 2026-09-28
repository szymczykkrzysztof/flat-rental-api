package com.komy.flatrentalapi.service;

import com.komy.flatrentalapi.config.TestcontainersConfiguration;
import com.komy.flatrentalapi.dto.auth.LoginRequest;
import com.komy.flatrentalapi.dto.auth.RefreshTokenRequest;
import com.komy.flatrentalapi.dto.auth.RegisterRequest;
import com.komy.flatrentalapi.exception.RefreshTokenReuseException;
import com.komy.flatrentalapi.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Not a {@code @DataJpaTest}: that slice wraps each test in a transaction that is always
 * rolled back at the end, which would hide the exact bug this test targets — whether the
 * revocation triggered by reuse detection actually commits when it runs underneath
 * {@link AuthService#refresh}. A plain {@code @SpringBootTest} lets every service's own
 * {@code @Transactional} boundary commit for real against the Testcontainers database, which
 * is what exposes the bug: the reuse-triggered revocation happens inside
 * {@code RefreshTokenService.rotateRefreshToken}, but it is {@code AuthService.refresh} —
 * one call frame further out — that owns the outermost transaction here.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class RefreshTokenServiceRotationIntegrationTest {

    @Autowired
    private AuthService authService;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Test
    void reuseDetectionThroughAuthServicePersistsRevocationOfOtherTokensDespiteThrownException() {
        authService.register(new RegisterRequest("reuse-test@example.com", "password123", "Reuse", "Test"));

        var loginResponse = authService.login(new LoginRequest("reuse-test@example.com", "password123"));
        String tokenA = loginResponse.refreshToken();

        var firstRefreshResponse = authService.refresh(new RefreshTokenRequest(tokenA));
        String tokenB = firstRefreshResponse.refreshToken();

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest(tokenA)))
                .isInstanceOf(RefreshTokenReuseException.class);

        var reloadedTokenB = refreshTokenRepository.findByToken(tokenB).orElseThrow();
        assertThat(reloadedTokenB.isRevoked())
                .as("replaying refresh token A after it was already rotated must revoke token B too, "
                        + "and that revocation must survive the RefreshTokenReuseException thrown by "
                        + "AuthService.refresh")
                .isTrue();
    }
}
