package com.project.notification_service.service;

import com.project.notification_service.dto.request.PreferenceCreateRequest;
import com.project.notification_service.dto.request.PreferenceUpdateRequest;
import com.project.notification_service.dto.response.GenericApiResponse;
import com.project.notification_service.dto.response.PreferenceGetResponse;
import com.project.notification_service.exception.ResourceNotFoundException;
import com.project.notification_service.model.NotificationPreference;
import com.project.notification_service.model.User;
import com.project.notification_service.repository.NotificationPreferenceRepository;
import com.project.notification_service.repository.UserRepository;
import com.project.notification_service.security.SecurityUtils;
import com.project.notification_service.security.UserPrincipal;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;

    @Transactional
    public GenericApiResponse createPreference(@Valid PreferenceCreateRequest preferenceCreateRequest) {
        // Get user data
        UserPrincipal userPrincipal = securityUtils.getCurrentUser();
        Long userId = userPrincipal.getId();
        User user = userRepository.getReferenceById(userId);

        NotificationPreference notificationPreference = NotificationPreference.builder()
                .channel(preferenceCreateRequest.getChannel())
                .eventType(preferenceCreateRequest.getEventType())
                .user(user)
                .createdBy(userId)
                .updatedBy(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .enabled(preferenceCreateRequest.getEnabled() == null || preferenceCreateRequest.getEnabled())
                .build();

        notificationPreferenceRepository.save(notificationPreference);
        log.info("Notification preference created successfully: {}", notificationPreference.getId());

        return GenericApiResponse.builder()
                .success("true")
                .message("Notification preference created successfully")
                .build();
    }

    @Transactional
    public GenericApiResponse updatePreference(@Valid PreferenceUpdateRequest preferenceUpdateRequest) {
        // Get userId
        UserPrincipal userPrincipal = securityUtils.getCurrentUser();
        Long userId = userPrincipal.getId();

        // Get preference details from DB
        NotificationPreference preference =
                notificationPreferenceRepository.findById(preferenceUpdateRequest.getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Preference not found"));

        if (preferenceUpdateRequest.getChannel() != null) {
            preference.setChannel(preferenceUpdateRequest.getChannel());
        }

        if (preferenceUpdateRequest.getEventType() != null) {
            preference.setEventType(preferenceUpdateRequest.getEventType());
        }

        if (preferenceUpdateRequest.getEnabled() != null) {
            preference.setEnabled(preferenceUpdateRequest.getEnabled());
        }

        preference.setUpdatedBy(userId);
        preference.setUpdatedAt(LocalDateTime.now());

        notificationPreferenceRepository.save(preference);

        return GenericApiResponse.builder()
                .success("true")
                .message("Preference Updated successfully")
                .build();
    }

    public List<PreferenceGetResponse> getPreferencesByUserId() {
        UserPrincipal userPrincipal = securityUtils.getCurrentUser();
        Long userId = userPrincipal.getId();

        return notificationPreferenceRepository.findByUserId(userId).stream()
                .map(preference -> PreferenceGetResponse.builder()
                        .id(preference.getId())
                        .user_id(preference.getUser().getId())
                        .channel(preference.getChannel())
                        .eventType(preference.getEventType())
                        .enabled(preference.getEnabled())
                        .createdAt(preference.getCreatedAt())
                        .updatedAt(preference.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
