package com.project.notification_service.service;

import com.project.notification_service.dto.request.UserAuthRequest;
import com.project.notification_service.dto.response.UserAuthResponse;
import com.project.notification_service.dto.request.UserLoginRequest;

import com.project.notification_service.exception.DuplicateResourceException;
import com.project.notification_service.exception.ResourceNotFoundException;
import com.project.notification_service.exception.UnauthorizedException;

import com.project.notification_service.model.User;
import com.project.notification_service.repository.UserRepository;
import com.project.notification_service.security.JwtTokenProvider;
import com.project.notification_service.security.UserPrincipal;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public UserAuthResponse createUser(UserAuthRequest userRequest) {

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new DuplicateResourceException("Email already exists");
        }

        User user = User.builder()
                .username(userRequest.getUsername())
                .email(userRequest.getEmail())
                .role(userRequest.getRole())
                .passwordHash(passwordEncoder.encode(userRequest.getPassword()))
                .createdAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String token = jwtTokenProvider.generateToken(userPrincipal);

        log.info("User created successfully: {}", user.getUsername());

        return UserAuthResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .role(userPrincipal.getAuthorities().iterator().next().getAuthority())
                .email(user.getEmail())
                .token(token)
                .build();
    }

    public UserAuthResponse loginUser(@Valid UserLoginRequest userLoginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userLoginRequest.getUsername(),
                        userLoginRequest.getPassword()
                ));

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtTokenProvider.generateToken(userPrincipal);

        return UserAuthResponse.builder()
                .userId(userPrincipal.getId())
                .username(userPrincipal.getUsername())
                .role(userPrincipal.getAuthorities().iterator().next().getAuthority())
                .email(userPrincipal.getEmail())
                .token(token)
                .build();
    }
}
