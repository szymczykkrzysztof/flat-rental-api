package com.komy.flatrentalapi.service;

import com.komy.flatrentalapi.entity.RefreshToken;
import com.komy.flatrentalapi.entity.User;
import com.komy.flatrentalapi.exception.InvalidCredentialsException;
import com.komy.flatrentalapi.exception.RefreshTokenReuseException;
import com.komy.flatrentalapi.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private RefreshTokenRevocationService refreshTokenRevocationService;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user() {
        User user = new User();
        user.setId(1L);
        return user;
    }

    private RefreshToken tokenFor(User user, String value, boolean revoked, Instant expiresAt) {
        var token = new RefreshToken();
        token.setToken(value);
        token.setUser(user);
        token.setRevoked(revoked);
        token.setExpiresAt(expiresAt);
        return token;
    }

    @Test
    void generateRefreshTokenSavesUnrevokedTokenWithFutureExpiry() {
        User user = user();
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken result = refreshTokenService.generateRefreshToken(user);

        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.isRevoked()).isFalse();
        assertThat(result.getToken()).isNotBlank();
        assertThat(result.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void rotateRefreshTokenThrowsWhenTokenNotFound() {
        when(refreshTokenRepository.findByToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken("missing"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void rotateRefreshTokenThrowsWhenExpiredAndDoesNotRevokeAnything() {
        User user = user();
        var expired = tokenFor(user, "expired", false, Instant.now().minus(1, ChronoUnit.DAYS));
        when(refreshTokenRepository.findByToken("expired")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken("expired"))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(refreshTokenRepository, never()).save(any());
        verify(refreshTokenRepository, never()).findByUserIdAndRevokedFalse(any());
    }

    @Test
    void rotateRefreshTokenRevokesOldTokenAndIssuesNewOneWhenValid() {
        User user = user();
        var oldToken = tokenFor(user, "old", false, Instant.now().plus(1, ChronoUnit.DAYS));
        when(refreshTokenRepository.findByToken("old")).thenReturn(Optional.of(oldToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        RefreshToken newToken = refreshTokenService.rotateRefreshToken("old");

        assertThat(oldToken.isRevoked()).isTrue();
        assertThat(newToken).isNotNull();
        assertThat(newToken.getToken()).isNotEqualTo("old");
        assertThat(newToken.isRevoked()).isFalse();
        assertThat(newToken.getUser()).isEqualTo(user);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, org.mockito.Mockito.times(2)).save(captor.capture());
        assertThat(captor.getAllValues()).extracting(RefreshToken::getToken).contains("old");
    }

    @Test
    void rotateRefreshTokenOnReuseDelegatesRevocationAndThrowsWithoutIssuingNewToken() {
        User user = user();
        var reusedToken = tokenFor(user, "reused", true, Instant.now().plus(1, ChronoUnit.DAYS));
        when(refreshTokenRepository.findByToken("reused")).thenReturn(Optional.of(reusedToken));

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken("reused"))
                .isInstanceOf(RefreshTokenReuseException.class);

        verify(refreshTokenRevocationService).revokeAllUserTokens(user);
        verify(refreshTokenRepository, never()).save(any());
    }
}
