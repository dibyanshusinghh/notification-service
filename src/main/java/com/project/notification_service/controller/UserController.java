package com.project.notification_service.controller;

import com.project.notification_service.dto.request.UserAuthRequest;
import com.project.notification_service.dto.response.UserAuthResponse;
import com.project.notification_service.dto.request.UserLoginRequest;
import com.project.notification_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "User registration and login — no JWT required")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and returns a JWT token. " +
                    "The `role` field must be one of: CLIENT, ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully, JWT returned"),
            @ApiResponse(responseCode = "400", description = "Validation failed — missing or invalid fields"),
            @ApiResponse(responseCode = "409", description = "Email already in use")
    })
    @SecurityRequirements
    @PostMapping("/register")
    public ResponseEntity<UserAuthResponse> registerUser(@Valid @RequestBody UserAuthRequest userRequest) {
        return ResponseEntity.ok(userService.createUser(userRequest));
    }

    @Operation(
            summary = "Login",
            description = "Authenticates a user by username and password and returns a JWT token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful, JWT returned"),
            @ApiResponse(responseCode = "400", description = "Validation failed — missing or invalid fields"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<UserAuthResponse> loginUser(@Valid @RequestBody UserLoginRequest userLoginRequest) {
        return ResponseEntity.ok(userService.loginUser(userLoginRequest));
    }
}
