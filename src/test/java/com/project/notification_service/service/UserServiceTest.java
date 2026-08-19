package com.project.notification_service.service;

import com.project.notification_service.dto.request.UserAuthRequest;
import com.project.notification_service.dto.request.UserLoginRequest;
import com.project.notification_service.dto.response.UserAuthResponse;
import com.project.notification_service.exception.DuplicateResourceException;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        when(userRepository.existsByEmail(userRequest.getEmail()))
                .thenReturn(false);
        when(passwordEncoder.encode(userRequest.getPassword()))
                .thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);
        when(jwtTokenProvider.generateToken(any(UserPrincipal.class)))
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

        verify(userRepository).existsByEmail(userRequest.getEmail());
        verify(passwordEncoder).encode(userRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtTokenProvider).generateToken(any(UserPrincipal.class));
    }

    @Test
    public void registerUser_EmailAlreadyExists() {
        // Arrange
        UserAuthRequest userRequest = new UserAuthRequest("testuser", "testuser@gmail.com", Role.CLIENT, "testpassword");

        when(userRepository.existsByEmail(userRequest.getEmail()))
                .thenReturn(true);

        // Act & Assert
        Assertions.assertThrows(DuplicateResourceException.class,
                () -> userService.createUser(userRequest));

        verify(userRepository).existsByEmail(userRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void loginUser_Success() {
        // Arrange
        UserLoginRequest loginRequest = new UserLoginRequest("testuser", "testpassword");

        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("testuser@gmail.com")
                .role(Role.CLIENT)
                .passwordHash("hashedpassword")
                .build();
        UserPrincipal userPrincipal = UserPrincipal.create(user);

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal())
                .thenReturn(userPrincipal);
        when(jwtTokenProvider.generateToken(any(UserPrincipal.class)))
                .thenReturn("jwt-token");

        // Act
        UserAuthResponse response = userService.loginUser(loginRequest);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(1L, response.getUserId());
        Assertions.assertEquals("testuser", response.getUsername());
        Assertions.assertEquals("testuser@gmail.com", response.getEmail());
        Assertions.assertEquals("ROLE_CLIENT", response.getRole());
        Assertions.assertEquals("jwt-token", response.getToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateToken(any(UserPrincipal.class));
    }
}
