package com.project.notification_service.service;


import com.project.notification_service.dto.request.TemplateCreateRequest;
import com.project.notification_service.dto.request.TemplateUpdateRequest;
import com.project.notification_service.dto.response.GenericApiResponse;
import com.project.notification_service.exception.ResourceNotFoundException;
import com.project.notification_service.model.NotificationTemplate;
import com.project.notification_service.model.enums.Channel;
import com.project.notification_service.model.enums.EventType;
import com.project.notification_service.repository.NotificationTemplateRepository;
import com.project.notification_service.security.SecurityUtils;
import com.project.notification_service.security.UserPrincipal;
import com.project.notification_service.utility.EmailTemplateUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationTemplateService {

    private final NotificationTemplateRepository notificationTemplateRepository;
    private final SecurityUtils securityUtils;
    private final EmailTemplateUtil emailTemplateUtil;

    @Transactional
    public GenericApiResponse createTemplate(@Valid TemplateCreateRequest templateCreateRequest) throws IOException {
        // Get userId
        UserPrincipal userPrincipal = securityUtils.getCurrentUser();
        Long userId = userPrincipal.getId();

        MultipartFile bodyTemplate = templateCreateRequest.getBodyTemplate();

        String html = new String(
                        bodyTemplate.getBytes(),
                        StandardCharsets.UTF_8);
        List<String> placeholders = emailTemplateUtil.extractPlaceholders(html);

        NotificationTemplate notificationTemplate = NotificationTemplate.builder()
                .eventType(templateCreateRequest.getEventType())
                .channel(templateCreateRequest.getChannel())
                .subjectTemplate(templateCreateRequest.getSubjectTemplate())
                .createdBy(userId)
                .updatedBy(userId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .bodyTemplate(html)
                .placeholders(String.join(",", placeholders))
                .build();

        notificationTemplateRepository.save(notificationTemplate);
        log.info("Notification template created successfully: {}", notificationTemplate.getId());

        return GenericApiResponse.builder()
                .success("true")
                .message("Notification template created successfully")
                .build();
    }

    @Transactional
    public GenericApiResponse updateTemplate(@Valid TemplateUpdateRequest templateUpdateRequest) throws IOException {
        // Get userId
        UserPrincipal userPrincipal = securityUtils.getCurrentUser();
        Long userId = userPrincipal.getId();

        NotificationTemplate notificationTemplate = notificationTemplateRepository.findById(templateUpdateRequest.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification template not found: " + templateUpdateRequest.getId()));

        if (templateUpdateRequest.getSubjectTemplate() != null) {
            notificationTemplate.setSubjectTemplate(templateUpdateRequest.getSubjectTemplate());
        }
        if (templateUpdateRequest.getBodyTemplate() != null) {
            MultipartFile bodyTemplate = templateUpdateRequest.getBodyTemplate();

            String html = new String(
                    bodyTemplate.getBytes(),
                    StandardCharsets.UTF_8);

            List<String> placeholders = emailTemplateUtil.extractPlaceholders(html);

            notificationTemplate.setBodyTemplate(html);
            notificationTemplate.setPlaceholders(String.join(",", placeholders));
        }
        notificationTemplate.setUpdatedAt(LocalDateTime.now());
        notificationTemplate.setUpdatedBy(userId);

        notificationTemplateRepository.save(notificationTemplate);
        log.info("Notification template updated successfully: {}", notificationTemplate.getId());

        return GenericApiResponse.builder()
                .success("true")
                .message("Notification template updated successfully")
                .build();
    }

    public List<NotificationTemplate> getTemplates(Channel channel, EventType eventType) {
        Specification<NotificationTemplate> spec = Specification.where((Specification<NotificationTemplate>) null);
        if (channel != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("channel"), channel));
        }
        if (eventType != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("eventType"), eventType));
        }

        return notificationTemplateRepository.findAll(spec);
    }
}
