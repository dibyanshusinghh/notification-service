package com.project.notification_service.dto.response;

import com.project.notification_service.model.enums.Channel;
import com.project.notification_service.model.enums.EventType;
import com.project.notification_service.model.enums.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private Channel channel;
    private LocalDateTime createdAt;
    private EventType eventType;
    private LocalDateTime sentAt;
    private NotificationStatus status;

}
