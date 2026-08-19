package com.project.notification_service.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiError {

    private LocalDateTime timestamp;

    private int status;

    private String errorCode;

    private String error;

    private String message;

    private String path;

    private Map<String, String> validationErrors;
}
