package com.project.notification_service.controller;

import com.project.notification_service.dto.request.PreferenceCreateRequest;
import com.project.notification_service.dto.request.PreferenceUpdateRequest;
import com.project.notification_service.dto.response.GenericApiResponse;
import com.project.notification_service.dto.response.PreferenceGetResponse;
import com.project.notification_service.service.NotificationPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notification/preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final NotificationPreferenceService notificationPreferenceService;

    @PostMapping("/")
    public ResponseEntity<GenericApiResponse> createPreference(@Valid @RequestBody PreferenceCreateRequest preferenceCreateRequest) {
        return ResponseEntity.ok(notificationPreferenceService.createPreference(preferenceCreateRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenericApiResponse> updatePreference(@PathVariable Long id ,@Valid @RequestBody PreferenceUpdateRequest preferenceUpdateRequest) {
        preferenceUpdateRequest.setId(id);
        return ResponseEntity.ok(notificationPreferenceService.updatePreference(preferenceUpdateRequest));
    }

    @GetMapping
    public ResponseEntity<List<PreferenceGetResponse>> getAllPreferences() {
        return ResponseEntity.ok(notificationPreferenceService.getPreferencesByUserId());
    }
}
