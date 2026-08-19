package com.project.notification_service.service;

import com.project.notification_service.dto.request.UserAuthRequest;
import com.project.notification_service.dto.response.UserAuthResponse;
import com.project.notification_service.model.User;
import com.project.notification_service.model.enums.Role;
import com.project.notification_service.repository.UserRepository;
import com.project.notification_service.security.JwtTokenProvider;
import com.project.notification_service.security.UserPrincipal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private UserService userService;

    @Test
    public void registerUser_Success() {
        // Arrange
        UserAuthRequest userRequest = new UserAuthRequest("testuser", "testuser@gmail.com", Role.CLIENT, "testpassword");

        User savedUser = User.builder()
                .id(1L)
                .username(userRequest.getUsername())
                .email(userRequest.getEmail())
                .role(userRequest.getRole())
                .passwordHash("hashedpassword")
                .build();

        Mockito.when(userRepository.existsByEmail(userRequest.getEmail()))
                .thenReturn(false);
        Mockito.when(passwordEncoder.encode(userRequest.getPassword()))
                .thenReturn("encodedPassword");
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenReturn(savedUser);
        Mockito.when(jwtTokenProvider.generateToken(Mockito.any(UserPrincipal.class)))
                .thenReturn("jwt-token");

        // Act
        UserAuthResponse response = userService.createUser(userRequest);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(1L, response.getUserId());
        Assertions.assertEquals("testuser", response.getUsername());
        Assertions.assertEquals("testuser@gmail.com", response.getEmail());
        Assertions.assertEquals("ROLE_CLIENT", response.getRole());
        Assertions.assertEquals("jwt-token", response.getToken());

        Mockito.verify(userRepository).existsByEmail(userRequest.getEmail());
        Mockito.verify(passwordEncoder).encode(userRequest.getPassword());
        Mockito.verify(userRepository).save(Mockito.any(User.class));
        Mockito.verify(jwtTokenProvider).generateToken(Mockito.any(UserPrincipal.class));
    }
}
