package com.project.notification_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.notification_service.dto.request.NotificationAddRequest;
import com.project.notification_service.dto.request.NotificationGetAllRequest;
import com.project.notification_service.dto.response.GenericApiResponse;
import com.project.notification_service.dto.response.NotificationResponse;
import com.project.notification_service.exception.ResourceNotFoundException;
import com.project.notification_service.model.Notification;
import com.project.notification_service.model.User;
import com.project.notification_service.model.enums.NotificationStatus;
import com.project.notification_service.repository.NotificationRepository;
import com.project.notification_service.repository.UserRepository;
import com.project.notification_service.security.SecurityUtils;
import com.project.notification_service.security.UserPrincipal;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, NotificationAddRequest> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final SecurityUtils securityUtils;

    @Transactional
    public GenericApiResponse addNotification(@Valid NotificationAddRequest notificationAddRequest) throws JsonProcessingException {
        // Get user details
        User user = userRepository.findById(notificationAddRequest.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + notificationAddRequest.getRecipientId()));

        // Logic to add a notification would go here
        Notification notification = Notification.builder()
                .channel(notificationAddRequest.getChannel())
                .eventType(notificationAddRequest.getEventType())
                .payload(objectMapper.writeValueAsString(notificationAddRequest.getPayload()))
                .user(user)
                .idempotencyKey(notificationAddRequest.getIdempotencyKey())
                .status(NotificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Notification notificationRecord = notificationRepository.save(notification);

        notificationAddRequest.setId(notificationRecord.getId());
        notificationAddRequest.setEmail(user.getEmail());

        // Publishes NotificationRequestedEvent to Kafka topic "notification-events"
        kafkaTemplate.send("notification-events", notificationAddRequest);

        return GenericApiResponse.builder()
                .success("true")
                .message("Notification added successfully")
                .build();
    }

    public Page<NotificationResponse> getNotifications(@Valid NotificationGetAllRequest notificationGetAllRequest) {
        // Get current user details
        UserPrincipal userPrincipal = securityUtils.getCurrentUser();

        Pageable pageable = PageRequest.of(
                notificationGetAllRequest.getPage(),
                notificationGetAllRequest.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Specification<Notification> spec = Specification.where(
                (root, query, cb) ->
                        cb.equal(root.get("user").get("id"), userPrincipal.getId())
        );

        if (notificationGetAllRequest.getStatus() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), notificationGetAllRequest.getStatus())
            );
        }

        Page<Notification> notifications =
                notificationRepository.findAll(
                        spec,
                        pageable
                );

        return notifications.map(notification -> NotificationResponse.builder()
                .id(notification.getId())
                .channel(notification.getChannel())
                .eventType(notification.getEventType())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .build()
        );
    }

    public NotificationResponse getNotificationById(Long id) {
        // Implementation for fetching notification by ID
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));

        return NotificationResponse.builder()
                .id(notification.getId())
                .channel(notification.getChannel())
                .eventType(notification.getEventType())
                .status(notification.getStatus())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
