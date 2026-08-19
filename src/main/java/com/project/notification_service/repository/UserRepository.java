package com.project.notification_service.repository;

import com.project.notification_service.model.User;
import io.micrometer.observation.ObservationFilter;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByEmailOrUsername(@Email(message = "Invalid email format") String email, String username);

    Optional<User> findByUsername(String username);

}
