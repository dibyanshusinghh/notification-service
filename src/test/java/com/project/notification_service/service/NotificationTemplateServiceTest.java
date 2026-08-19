package com.project.notification_service.service;

import com.project.notification_service.dto.request.TemplateCreateRequest;
import com.project.notification_service.dto.request.TemplateUpdateRequest;
import com.project.notification_service.dto.response.GenericApiResponse;
import com.project.notification_service.exception.ResourceNotFoundException;
import com.project.notification_service.model.NotificationTemplate;
import com.project.notification_service.model.User;
import com.project.notification_service.model.enums.Channel;
import com.project.notification_service.model.enums.EventType;
import com.project.notification_service.model.enums.Role;
import com.project.notification_service.repository.NotificationTemplateRepository;
import com.project.notification_service.security.SecurityUtils;
import com.project.notification_service.security.UserPrincipal;
import com.project.notification_service.utility.EmailTemplateUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationTemplateServiceTest {

    @Mock
    private NotificationTemplateRepository notificationTemplateRepository;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private EmailTemplateUtil emailTemplateUtil;
    @InjectMocks
    private NotificationTemplateService notificationTemplateService;

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
    public void createTemplate_Success() throws Exception {
        // Arrange
        UserPrincipal userPrincipal = buildUserPrincipal();
        String html = "<p>Hello {{name}}</p>";

        MultipartFile bodyTemplate = mock(MultipartFile.class);
        when(bodyTemplate.getBytes()).thenReturn(html.getBytes(StandardCharsets.UTF_8));
        when(emailTemplateUtil.extractPlaceholders(html)).thenReturn(List.of("name"));

        TemplateCreateRequest request = TemplateCreateRequest.builder()
                .eventType(EventType.ORDER_UPDATE)
                .channel(Channel.EMAIL)
                .subjectTemplate("Order Update")
                .bodyTemplate(bodyTemplate)
                .build();

        when(securityUtils.getCurrentUser()).thenReturn(userPrincipal);
        when(notificationTemplateRepository.save(any(NotificationTemplate.class)))
                .thenReturn(NotificationTemplate.builder().id(1L).build());

        // Act
        GenericApiResponse response = notificationTemplateService.createTemplate(request);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals("true", response.getSuccess());
        Assertions.assertEquals("Notification template created successfully", response.getMessage());

        verify(securityUtils).getCurrentUser();
        verify(emailTemplateUtil).extractPlaceholders(html);
        verify(notificationTemplateRepository).save(any(NotificationTemplate.class));
    }

    @Test
    public void updateTemplate_Success() throws Exception {
        // Arrange
        UserPrincipal userPrincipal = buildUserPrincipal();

        NotificationTemplate existing = NotificationTemplate.builder()
                .id(1L)
                .eventType(EventType.DAILY_UPDATE)
                .channel(Channel.EMAIL)
                .subjectTemplate("Old Subject")
                .build();

        String html = "<p>Updated {{content}}</p>";
        MultipartFile bodyTemplate = mock(MultipartFile.class);
        when(bodyTemplate.getBytes()).thenReturn(html.getBytes(StandardCharsets.UTF_8));
        when(emailTemplateUtil.extractPlaceholders(html)).thenReturn(List.of("content"));

        TemplateUpdateRequest request = TemplateUpdateRequest.builder()
                .id(1L)
                .subjectTemplate("New Subject")
                .bodyTemplate(bodyTemplate)
                .build();

        when(securityUtils.getCurrentUser()).thenReturn(userPrincipal);
        when(notificationTemplateRepository.findById(1L))
                .thenReturn(Optional.of(existing));
        when(notificationTemplateRepository.save(any(NotificationTemplate.class)))
                .thenReturn(existing);

        // Act
        GenericApiResponse response = notificationTemplateService.updateTemplate(request);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals("true", response.getSuccess());
        Assertions.assertEquals("Notification template updated successfully", response.getMessage());

        verify(notificationTemplateRepository).findById(1L);
        verify(emailTemplateUtil).extractPlaceholders(html);
        verify(notificationTemplateRepository).save(any(NotificationTemplate.class));
    }

    @Test
    public void updateTemplate_NotFound() {
        // Arrange
        UserPrincipal userPrincipal = buildUserPrincipal();
        TemplateUpdateRequest request = TemplateUpdateRequest.builder()
                .id(99L)
                .subjectTemplate("New Subject")
                .build();

        when(securityUtils.getCurrentUser()).thenReturn(userPrincipal);
        when(notificationTemplateRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> notificationTemplateService.updateTemplate(request));

        verify(notificationTemplateRepository).findById(99L);
        verify(notificationTemplateRepository, never()).save(any());
    }

    @Test
    public void getTemplates_NoFilters() {
        // Arrange
        NotificationTemplate template = NotificationTemplate.builder()
                .id(1L)
                .channel(Channel.EMAIL)
                .eventType(EventType.ORDER_UPDATE)
                .subjectTemplate("Order Update")
                .build();

        when(notificationTemplateRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(template));

        // Act
        List<NotificationTemplate> result = notificationTemplateService.getTemplates(null, null);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(Channel.EMAIL, result.get(0).getChannel());
        Assertions.assertEquals(EventType.ORDER_UPDATE, result.get(0).getEventType());

        verify(notificationTemplateRepository).findAll(any(Specification.class));
    }

    @Test
    public void getTemplates_WithChannelFilter() {
        // Arrange
        NotificationTemplate template = NotificationTemplate.builder()
                .id(1L)
                .channel(Channel.EMAIL)
                .eventType(EventType.ORDER_UPDATE)
                .build();

        when(notificationTemplateRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(template));

        // Act
        List<NotificationTemplate> result = notificationTemplateService.getTemplates(Channel.EMAIL, null);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(Channel.EMAIL, result.get(0).getChannel());

        verify(notificationTemplateRepository).findAll(any(Specification.class));
    }
}
