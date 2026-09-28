package com.komy.flatrentalapi.security;

import com.komy.flatrentalapi.entity.User;
import com.komy.flatrentalapi.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public CurrentUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        String email = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));
    }
}