package com.project.notification_service.controller;

import com.project.notification_service.dto.request.PreferenceCreateRequest;
import com.project.notification_service.dto.request.PreferenceUpdateRequest;
import com.project.notification_service.dto.response.GenericApiResponse;
import com.project.notification_service.dto.response.PreferenceGetResponse;
import com.project.notification_service.service.NotificationPreferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notification Preferences", description = "Manage per-user channel/event notification preferences — JWT required")
@RestController
@RequestMapping("/api/notification/preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final NotificationPreferenceService notificationPreferenceService;

    @Operation(
            summary = "Create a notification preference",
            description = "Creates a new preference for the authenticated user, specifying a channel " +
                    "(EMAIL, IN_APP, PUSH) and event type (DAILY_UPDATE, WEEKLY_UPDATE, COMMENT_UPDATE, " +
                    "MEET_TRANSCRIPT, ORDER_UPDATE). The `enabled` field defaults to true if omitted."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preference created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed — missing or invalid fields"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @PostMapping("/")
    public ResponseEntity<GenericApiResponse> createPreference(
            @Valid @RequestBody PreferenceCreateRequest preferenceCreateRequest) {
        return ResponseEntity.ok(notificationPreferenceService.createPreference(preferenceCreateRequest));
    }

    @Operation(
            summary = "Update a notification preference",
            description = "Partially updates an existing preference by its ID. Only the fields provided " +
                    "in the request body are updated; omitted fields are left unchanged."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preference updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed — invalid field values"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "Preference not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<GenericApiResponse> updatePreference(
            @Parameter(description = "Database ID of the preference to update") @PathVariable Long id,
            @Valid @RequestBody PreferenceUpdateRequest preferenceUpdateRequest) {
        preferenceUpdateRequest.setId(id);
        return ResponseEntity.ok(notificationPreferenceService.updatePreference(preferenceUpdateRequest));
    }

    @Operation(
            summary = "Get all preferences for the current user",
            description = "Returns all notification preferences belonging to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of preferences returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @GetMapping
    public ResponseEntity<List<PreferenceGetResponse>> getAllPreferences() {
        return ResponseEntity.ok(notificationPreferenceService.getPreferencesByUserId());
    }
}
