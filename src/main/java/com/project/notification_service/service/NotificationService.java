package com.project.notification_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.notification_service.dto.request.NotificationAddRequest;
import com.project.notification_service.dto.response.GenericApiResponse;
import com.project.notification_service.exception.ResourceNotFoundException;
import com.project.notification_service.model.Notification;
import com.project.notification_service.model.User;
import com.project.notification_service.model.enums.NotificationStatus;
import com.project.notification_service.repository.NotificationRepository;
import com.project.notification_service.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, NotificationAddRequest> kafkaTemplate;
    private final ObjectMapper objectMapper;

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

}
