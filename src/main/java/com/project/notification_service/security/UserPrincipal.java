package com.project.notification_service.security;

import com.project.notification_service.model.User;
import com.project.notification_service.model.enums.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Immutable Spring Security principal wrapping the persisted {@link User}.
 * Stored in the {@code SecurityContext} after JWT validation.
 */
@Getter
public class UserPrincipal implements UserDetails {

    private final Long   id;
    private final String username;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    private UserPrincipal(Long id, String username, String email,
                          String password,
                          Collection<? extends GrantedAuthority> authorities) {
        this.id          = id;
        this.username    = username;
        this.email       = email;
        this.password    = password;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                authorities
        );
    }

    /** Convenience check used in service-layer role branching. */
    public boolean isAdmin() {
        return authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}
