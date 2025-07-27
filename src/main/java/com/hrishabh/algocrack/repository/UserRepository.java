package com.hrishabh.algocrack.repository;

import com.hrishabh.algocrackentityservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByEmail(String email);

    Optional<User> findByEmail(String email);

    boolean existsByUserId(String userId);
}
