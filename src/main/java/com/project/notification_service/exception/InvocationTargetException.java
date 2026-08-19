package com.project.notification_service.exception;

import org.springframework.http.HttpStatus;

public class InvocationTargetException extends ApiException {
    public InvocationTargetException(String message) {
        super(HttpStatus.BAD_REQUEST, "INVOCATION_TARGET_EXCEPTION", message);
    }
}
