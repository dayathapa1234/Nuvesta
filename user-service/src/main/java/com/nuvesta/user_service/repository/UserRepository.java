package com.nuvesta.user_service.repository;

import com.nuvesta.user_service.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserAccount, String> {
    Optional<UserAccount> findByEmail(String email);
    boolean existsByEmail(String email);
}
