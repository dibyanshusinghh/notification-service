package com.project.notification_service.exception;

import org.springframework.http.HttpStatus;

public class EmailSendingException extends ApiException {
    public EmailSendingException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "EMAIL_FAILED", message);
    }
}
