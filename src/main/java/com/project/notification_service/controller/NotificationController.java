package com.project.notification_service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.notification_service.dto.request.NotificationAddRequest;
import com.project.notification_service.dto.response.GenericApiResponse;
import com.project.notification_service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<GenericApiResponse> addNotification(@Valid @RequestBody NotificationAddRequest notificationAddRequest) throws JsonProcessingException {
        return ResponseEntity.ok(notificationService.addNotification(notificationAddRequest));
    }
}
