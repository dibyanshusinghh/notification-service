package com.project.notification_service.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.notification_service.dto.request.NotificationAddRequest;
import com.project.notification_service.dto.request.NotificationGetAllRequest;
import com.project.notification_service.dto.response.GenericApiResponse;
import com.project.notification_service.dto.response.NotificationResponse;
import com.project.notification_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notifications", description = "Create and query notifications — JWT required")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "Create a notification",
            description = "Saves a PENDING notification record, then publishes a NotificationAddRequest event " +
                    "to the Kafka topic `notification-events`. The Kafka consumer dispatches the actual " +
                    "delivery (e.g. email) asynchronously."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification queued successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed — missing or invalid fields"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "Recipient user not found")
    })
    @PostMapping
    public ResponseEntity<GenericApiResponse> addNotification(
            @Valid @RequestBody NotificationAddRequest notificationAddRequest) throws JsonProcessingException {
        return ResponseEntity.ok(notificationService.addNotification(notificationAddRequest));
    }

    @Operation(
            summary = "List notifications for the current user",
            description = "Returns a paginated list of notifications belonging to the authenticated user. " +
                    "Optionally filter by delivery status."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of notifications returned"),
            @ApiResponse(responseCode = "400", description = "Validation failed — invalid pagination or status value"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @Parameter(description = "Pagination and optional status filter. " +
                    "`status` valid values: PENDING, SENT, FAILED")
            @Valid @RequestParam NotificationGetAllRequest notificationGetAllRequest) {
        return ResponseEntity.ok(notificationService.getNotifications(notificationGetAllRequest));
    }

    @Operation(
            summary = "Get a notification by ID",
            description = "Returns a single notification record by its database ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification found and returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getNotificationById(
            @Parameter(description = "Database ID of the notification") @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationById(id));
    }
}
