package com.project.notification_service.service;

import com.project.notification_service.dto.request.PreferenceCreateRequest;
import com.project.notification_service.dto.request.PreferenceUpdateRequest;
import com.project.notification_service.dto.response.GenericApiResponse;
import com.project.notification_service.dto.response.PreferenceGetResponse;
import com.project.notification_service.exception.ResourceNotFoundException;
import com.project.notification_service.model.NotificationPreference;
import com.project.notification_service.model.User;
import com.project.notification_service.model.enums.Channel;
import com.project.notification_service.model.enums.EventType;
import com.project.notification_service.model.enums.Role;
import com.project.notification_service.repository.NotificationPreferenceRepository;
import com.project.notification_service.repository.UserRepository;
import com.project.notification_service.security.SecurityUtils;
import com.project.notification_service.security.UserPrincipal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationPreferenceServiceTest {

    @Mock
    private NotificationPreferenceRepository notificationPreferenceRepository;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private NotificationPreferenceService notificationPreferenceService;

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
    public void createPreference_Success() {
        // Arrange
        UserPrincipal userPrincipal = buildUserPrincipal();
        PreferenceCreateRequest request = new PreferenceCreateRequest(Channel.EMAIL, EventType.ORDER_UPDATE, true);
        User user = User.builder().id(1L).build();

        when(securityUtils.getCurrentUser()).thenReturn(userPrincipal);
        when(userRepository.getReferenceById(1L)).thenReturn(user);
        when(notificationPreferenceRepository.save(any(NotificationPreference.class)))
                .thenReturn(NotificationPreference.builder().id(1L).build());

        // Act
        GenericApiResponse response = notificationPreferenceService.createPreference(request);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals("true", response.getSuccess());
        Assertions.assertEquals("Notification preference created successfully", response.getMessage());

        verify(securityUtils).getCurrentUser();
        verify(userRepository).getReferenceById(1L);
        verify(notificationPreferenceRepository).save(any(NotificationPreference.class));
    }

    @Test
    public void updatePreference_Success() {
        // Arrange
        UserPrincipal userPrincipal = buildUserPrincipal();
        PreferenceUpdateRequest request = PreferenceUpdateRequest.builder()
                .id(1L)
                .channel(Channel.EMAIL)
                .eventType(EventType.ORDER_UPDATE)
                .enabled(false)
                .build();

        NotificationPreference preference = NotificationPreference.builder()
                .id(1L)
                .channel(Channel.PUSH)
                .eventType(EventType.DAILY_UPDATE)
                .enabled(true)
                .build();

        when(securityUtils.getCurrentUser()).thenReturn(userPrincipal);
        when(notificationPreferenceRepository.findById(1L))
                .thenReturn(Optional.of(preference));
        when(notificationPreferenceRepository.save(any(NotificationPreference.class)))
                .thenReturn(preference);

        // Act
        GenericApiResponse response = notificationPreferenceService.updatePreference(request);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals("true", response.getSuccess());
        Assertions.assertEquals("Preference Updated successfully", response.getMessage());

        verify(notificationPreferenceRepository).findById(1L);
        verify(notificationPreferenceRepository).save(any(NotificationPreference.class));
    }

    @Test
    public void updatePreference_NotFound() {
        // Arrange
        UserPrincipal userPrincipal = buildUserPrincipal();
        PreferenceUpdateRequest request = PreferenceUpdateRequest.builder()
                .id(99L)
                .enabled(false)
                .build();

        when(securityUtils.getCurrentUser()).thenReturn(userPrincipal);
        when(notificationPreferenceRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> notificationPreferenceService.updatePreference(request));

        verify(notificationPreferenceRepository).findById(99L);
        verify(notificationPreferenceRepository, never()).save(any());
    }

    @Test
    public void getPreferencesByUserId_Success() {
        // Arrange
        UserPrincipal userPrincipal = buildUserPrincipal();
        User user = User.builder().id(1L).build();

        NotificationPreference preference = NotificationPreference.builder()
                .id(1L)
                .user(user)
                .channel(Channel.EMAIL)
                .eventType(EventType.ORDER_UPDATE)
                .enabled(true)
                .build();

        when(securityUtils.getCurrentUser()).thenReturn(userPrincipal);
        when(notificationPreferenceRepository.findByUserId(1L))
                .thenReturn(List.of(preference));

        // Act
        List<PreferenceGetResponse> response = notificationPreferenceService.getPreferencesByUserId();

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals(1L, response.get(0).getId());
        Assertions.assertEquals(Channel.EMAIL, response.get(0).getChannel());
        Assertions.assertEquals(EventType.ORDER_UPDATE, response.get(0).getEventType());
        Assertions.assertTrue(response.get(0).getEnabled());

        verify(securityUtils).getCurrentUser();
        verify(notificationPreferenceRepository).findByUserId(1L);
    }
}
