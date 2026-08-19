package com.project.notification_service.exception;

import org.springframework.http.HttpStatus;

public class JsonProcessingException extends ApiException {
    public JsonProcessingException(String message) {
        super(HttpStatus.BAD_REQUEST, "JSON_EXCEPTION", message);
    }
}
