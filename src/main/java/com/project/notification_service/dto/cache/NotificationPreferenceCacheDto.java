package com.project.notification_service.dto.cache;

import com.project.notification_service.model.enums.Channel;
import com.project.notification_service.model.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceCacheDto {

    private Long id;

    private Long userId;

    private Channel channel;

    private EventType eventType;

    private Boolean enabled;

}