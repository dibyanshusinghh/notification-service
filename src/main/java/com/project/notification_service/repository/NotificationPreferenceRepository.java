package com.project.notification_service.repository;

import com.project.notification_service.model.NotificationPreference;
import com.project.notification_service.model.enums.Channel;
import com.project.notification_service.model.enums.EventType;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    List<NotificationPreference> findByUserId(Long userId);

    NotificationPreference findByUserIdAndChannelAndEventType(@NotNull(message = "Recipient ID is required") Long recipientId, @NotNull(message = "Channel is required") Channel channel, @NotNull(message = "Event type is required") EventType eventType);
}
