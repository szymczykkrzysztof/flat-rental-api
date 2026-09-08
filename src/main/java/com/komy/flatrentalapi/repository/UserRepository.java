package com.komy.flatrentalapi.repository;

import com.komy.flatrentalapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
