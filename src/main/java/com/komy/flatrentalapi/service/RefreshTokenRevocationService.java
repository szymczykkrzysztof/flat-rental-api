package com.komy.flatrentalapi.service;

import com.komy.flatrentalapi.entity.User;
import com.komy.flatrentalapi.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Revokes a user's refresh tokens in its own transaction, independent of whatever
 * transaction the caller is running in. Reuse detection needs this: it revokes the user's
 * other tokens and then throws, and that revocation must survive regardless of how many
 * {@code @Transactional} layers end up wrapping the call site in the future. A plain call
 * from {@code RefreshTokenService} would not get this independence — a transactional method
 * called on {@code this} bypasses the proxy that {@code REQUIRES_NEW} relies on — so this
 * lives on a separate bean instead.
 */
@Service
public class RefreshTokenRevocationService {
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenRevocationService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeAllUserTokens(User user) {
        var tokens = refreshTokenRepository.findByUserIdAndRevokedFalse(user.getId());
        tokens.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);
    }
}
