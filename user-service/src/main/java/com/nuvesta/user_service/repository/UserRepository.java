package com.nuvesta.user_service.repository;

import com.nuvesta.user_service.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserAccount, String> {
    Optional<UserAccount> findByEmail(String email);
    boolean existsByEmail(String email);
}
