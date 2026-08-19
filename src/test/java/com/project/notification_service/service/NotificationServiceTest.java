package com.project.notification_service.service;

import com.project.notification_service.dto.request.NotificationAddRequest;
import com.project.notification_service.dto.request.NotificationGetAllRequest;
import com.project.notification_service.dto.response.GenericApiResponse;
import com.project.notification_service.dto.response.NotificationResponse;
import com.project.notification_service.exception.ResourceNotFoundException;
import com.project.notification_service.model.Notification;
import com.project.notification_service.model.User;
import com.project.notification_service.model.enums.Channel;
import com.project.notification_service.model.enums.EventType;
import com.project.notification_service.model.enums.NotificationStatus;
import com.project.notification_service.model.enums.Role;
import com.project.notification_service.repository.NotificationRepository;
import com.project.notification_service.repository.UserRepository;
import com.project.notification_service.security.SecurityUtils;
import com.project.notification_service.security.UserPrincipal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private KafkaTemplate<String, NotificationAddRequest> kafkaTemplate;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private SecurityUtils securityUtils;
    @InjectMocks
    private NotificationService notificationService;

    private UserPrincipal buildUserPrincipal() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("testuser@gmail.com")
                .role(Role.CLIENT)
                .passwordHash("hashedpassword")
                .build();
        return UserPrincipal.create(user);
    }

    @Test
    public void addNotification_Success() throws Exception {
        // Arrange
        NotificationAddRequest request = NotificationAddRequest.builder()
                .recipientId(1L)
                .channel(Channel.EMAIL)
                .eventType(EventType.ORDER_UPDATE)
                .payload(Map.of("orderId", "123"))
                .idempotencyKey("key-001")
                .build();

        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("testuser@gmail.com")
                .role(Role.CLIENT)
                .passwordHash("hashedpassword")
                .build();

        Notification savedNotification = Notification.builder()
                .id(10L)
                .channel(Channel.EMAIL)
                .eventType(EventType.ORDER_UPDATE)
                .status(NotificationStatus.PENDING)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"orderId\":\"123\"}");
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);

        // Act
        GenericApiResponse response = notificationService.addNotification(request);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals("true", response.getSuccess());
        Assertions.assertEquals("Notification added successfully", response.getMessage());

        verify(userRepository).findById(1L);
        verify(notificationRepository).save(any(Notification.class));
        verify(kafkaTemplate).send("notification-events", request);
    }

    @Test
    public void addNotification_UserNotFound() throws Exception {
        // Arrange
        NotificationAddRequest request = NotificationAddRequest.builder()
                .recipientId(99L)
                .channel(Channel.EMAIL)
                .eventType(EventType.ORDER_UPDATE)
                .payload(Map.of("orderId", "123"))
                .idempotencyKey("key-002")
                .build();

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> notificationService.addNotification(request));

        verify(userRepository).findById(99L);
        verify(notificationRepository, never()).save(any(Notification.class));
        verify(kafkaTemplate, never()).send(any(), any(NotificationAddRequest.class));
    }

    @Test
    public void getNotifications_Success() {
        // Arrange
        UserPrincipal userPrincipal = buildUserPrincipal();
        NotificationGetAllRequest request = NotificationGetAllRequest.builder()
                .page(0)
                .size(10)
                .build();

        Notification notification = Notification.builder()
                .id(1L)
                .channel(Channel.EMAIL)
                .eventType(EventType.ORDER_UPDATE)
                .status(NotificationStatus.SENT)
                .createdAt(LocalDateTime.now())
                .build();

        Page<Notification> page = new PageImpl<>(List.of(notification));

        when(securityUtils.getCurrentUser()).thenReturn(userPrincipal);
        when(notificationRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // Act
        Page<NotificationResponse> result = notificationService.getNotifications(request);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(1L, result.getContent().get(0).getId());
        Assertions.assertEquals(Channel.EMAIL, result.getContent().get(0).getChannel());
        Assertions.assertEquals(NotificationStatus.SENT, result.getContent().get(0).getStatus());

        verify(securityUtils).getCurrentUser();
        verify(notificationRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    public void getNotifications_WithStatusFilter() {
        // Arrange
        UserPrincipal userPrincipal = buildUserPrincipal();
        NotificationGetAllRequest request = NotificationGetAllRequest.builder()
                .page(0)
                .size(10)
                .status(NotificationStatus.SENT)
                .build();

        Notification notification = Notification.builder()
                .id(2L)
                .channel(Channel.EMAIL)
                .eventType(EventType.ORDER_UPDATE)
                .status(NotificationStatus.SENT)
                .createdAt(LocalDateTime.now())
                .build();

        Page<Notification> page = new PageImpl<>(List.of(notification));

        when(securityUtils.getCurrentUser()).thenReturn(userPrincipal);
        when(notificationRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        // Act
        Page<NotificationResponse> result = notificationService.getNotifications(request);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals(NotificationStatus.SENT, result.getContent().get(0).getStatus());

        verify(securityUtils).getCurrentUser();
        verify(notificationRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    public void getNotificationById_Success() {
        // Arrange
        Notification notification = Notification.builder()
                .id(1L)
                .channel(Channel.EMAIL)
                .eventType(EventType.ORDER_UPDATE)
                .status(NotificationStatus.SENT)
                .createdAt(LocalDateTime.now())
                .build();

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        // Act
        NotificationResponse response = notificationService.getNotificationById(1L);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(1L, response.getId());
        Assertions.assertEquals(Channel.EMAIL, response.getChannel());
        Assertions.assertEquals(EventType.ORDER_UPDATE, response.getEventType());
        Assertions.assertEquals(NotificationStatus.SENT, response.getStatus());

        verify(notificationRepository).findById(1L);
    }

    @Test
    public void getNotificationById_NotFound() {
        // Arrange
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> notificationService.getNotificationById(99L));

        verify(notificationRepository).findById(99L);
    }
}
