package com.project.notification_service.model;

import com.project.notification_service.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // mappedBy points to the 'user' field inside the NotificationPreference class
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NotificationPreference> preferences = new ArrayList<>();

    // mappedBy points to the 'user' field inside the Notification class
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();

    // Helper methods to keep both sides of the relationship synchronized in memory
    public void addPreference(NotificationPreference preference) {
        preferences.add(preference);
        preference.setUser(this);
    }

    public void removePreference(NotificationPreference preference) {
        preferences.remove(preference);
        preference.setUser(null);
    }
}
