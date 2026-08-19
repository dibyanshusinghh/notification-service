package com.project.notification_service.controller;

import com.project.notification_service.dto.request.TemplateCreateRequest;
import com.project.notification_service.dto.request.TemplateUpdateRequest;
import com.project.notification_service.dto.response.GenericApiResponse;
import com.project.notification_service.model.NotificationTemplate;
import com.project.notification_service.model.enums.Channel;
import com.project.notification_service.model.enums.EventType;
import com.project.notification_service.service.NotificationTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Tag(name = "Notification Templates", description = "Manage HTML email templates — JWT + ROLE_ADMIN required")
@RestController
@RequestMapping("/api/notification/templates")
@RequiredArgsConstructor
public class NotificationTemplateController {

    private final NotificationTemplateService notificationTemplateService;

    @Operation(
            summary = "Create a notification template",
            description = "Creates a new HTML email template for a given channel and event type. " +
                    "The `bodyTemplate` field is an HTML file upload (multipart/form-data). " +
                    "Placeholders in the HTML must use `{{placeholderName}}` syntax. " +
                    "Restricted to users with ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed — missing or invalid fields"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions — ROLE_ADMIN required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GenericApiResponse> createTemplate(
            @Valid @ModelAttribute TemplateCreateRequest templateCreateRequest) throws IOException {
        return ResponseEntity.ok(notificationTemplateService.createTemplate(templateCreateRequest));
    }

    @Operation(
            summary = "Update a notification template",
            description = "Partially updates an existing template by its ID. Only the fields provided " +
                    "are updated; omitted fields are left unchanged. " +
                    "Restricted to users with ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Template updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed — invalid field values"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions — ROLE_ADMIN required"),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GenericApiResponse> updateTemplate(
            @Parameter(description = "Database ID of the template to update") @PathVariable Long id,
            @Valid @ModelAttribute TemplateUpdateRequest templateUpdateRequest) throws IOException {
        templateUpdateRequest.setId(id);
        return ResponseEntity.ok(notificationTemplateService.updateTemplate(templateUpdateRequest));
    }

    @Operation(
            summary = "List notification templates",
            description = "Returns all templates, optionally filtered by channel and/or event type. " +
                    "Restricted to users with ROLE_ADMIN."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of templates returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions — ROLE_ADMIN required")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<NotificationTemplate>> getTemplates(
            @Parameter(description = "Filter by channel. Valid values: EMAIL, IN_APP, PUSH")
            @RequestParam(required = false) Channel channel,
            @Parameter(description = "Filter by event type. Valid values: DAILY_UPDATE, WEEKLY_UPDATE, COMMENT_UPDATE, MEET_TRANSCRIPT, ORDER_UPDATE")
            @RequestParam(required = false) EventType eventType) {
        return ResponseEntity.ok(notificationTemplateService.getTemplates(channel, eventType));
    }
}
