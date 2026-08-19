package com.project.notification_service.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

//    @Email(message = "Invalid email format")
//    private String email;

    @NotBlank(message = "Password is required")
    private String password;

//    @AssertTrue(message = "Either username or email must be provided")
//    public boolean isUsernameOrEmailPresent() {
//        return (username != null && !username.isBlank()) ||
//                (email != null && !email.isBlank());
//    }
}
