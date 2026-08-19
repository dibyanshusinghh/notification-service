package com.project.notification_service.exception;

import org.springframework.http.HttpStatus;

public class IOException extends ApiException {
    public IOException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "IO_ERROR", message);
    }
}
