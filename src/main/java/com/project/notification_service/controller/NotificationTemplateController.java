package com.project.notification_service.controller;

import com.project.notification_service.dto.request.TemplateCreateRequest;
import com.project.notification_service.dto.request.TemplateUpdateRequest;
import com.project.notification_service.dto.response.GenericApiResponse;
import com.project.notification_service.model.NotificationTemplate;
import com.project.notification_service.model.enums.Channel;
import com.project.notification_service.model.enums.EventType;
import com.project.notification_service.service.NotificationTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/notification/templates")
@RequiredArgsConstructor
public class NotificationTemplateController {

    private final NotificationTemplateService notificationTemplateService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GenericApiResponse> createTemplate(@Valid @ModelAttribute TemplateCreateRequest templateCreateRequest) throws IOException {
        return ResponseEntity.ok(notificationTemplateService.createTemplate(templateCreateRequest));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GenericApiResponse> updateTemplate(@PathVariable Long id, @Valid @ModelAttribute TemplateUpdateRequest templateUpdateRequest) throws IOException {
        templateUpdateRequest.setId(id);
        return ResponseEntity.ok(notificationTemplateService.updateTemplate(templateUpdateRequest));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<NotificationTemplate>> getTemplates(
            @RequestParam(required = false) Channel channel,
            @RequestParam(required = false) EventType eventType) {
        return ResponseEntity.ok(notificationTemplateService.getTemplates(channel, eventType));
    }
}
