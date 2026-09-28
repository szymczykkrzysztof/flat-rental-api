package com.komy.flatrentalapi.service;

import com.komy.flatrentalapi.entity.RefreshToken;
import com.komy.flatrentalapi.entity.User;
import com.komy.flatrentalapi.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenRevocationServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenRevocationService refreshTokenRevocationService;

    @Test
    void revokeAllUserTokensRevokesOnlyCurrentlyActiveTokens() {
        User user = new User();
        user.setId(1L);
        var active = new RefreshToken();
        active.setToken("active");
        active.setUser(user);
        active.setRevoked(false);
        active.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));
        when(refreshTokenRepository.findByUserIdAndRevokedFalse(1L)).thenReturn(List.of(active));

        refreshTokenRevocationService.revokeAllUserTokens(user);

        assertThat(active.isRevoked()).isTrue();
        verify(refreshTokenRepository).saveAll(List.of(active));
    }
}
