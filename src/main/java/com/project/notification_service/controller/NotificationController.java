package com.project.notification_service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.notification_service.dto.request.NotificationAddRequest;
import com.project.notification_service.dto.request.NotificationGetAllRequest;
import com.project.notification_service.dto.response.GenericApiResponse;
import com.project.notification_service.dto.response.NotificationResponse;
import com.project.notification_service.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<GenericApiResponse> addNotification(@Valid @RequestBody NotificationAddRequest notificationAddRequest) throws JsonProcessingException {
        return ResponseEntity.ok(notificationService.addNotification(notificationAddRequest));
    }

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(@Valid @RequestParam NotificationGetAllRequest notificationGetAllRequest) {
        return ResponseEntity.ok(notificationService.getNotifications(notificationGetAllRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }
}
