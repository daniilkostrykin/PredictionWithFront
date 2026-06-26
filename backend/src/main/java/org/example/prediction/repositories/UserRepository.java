package org.example.prediction.repositories;

import org.example.prediction.models.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.example.prediction.models.enums.UserRole;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Page<User> findAllByUsernameContainingIgnoreCaseAndRoleIsNot(String search, UserRole role, Pageable pageable);
    Page<User> findAllByUsernameContainingIgnoreCase(String search, Pageable pageable);
    Optional<User> findByEmail(String email);
}