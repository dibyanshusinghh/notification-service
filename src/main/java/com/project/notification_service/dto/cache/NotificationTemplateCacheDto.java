package com.project.notification_service.dto.cache;

import com.project.notification_service.model.enums.Channel;
import com.project.notification_service.model.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateCacheDto {

    private Long id;

    private Channel channel;

    private EventType eventType;

    private String subjectTemplate;

    private String bodyTemplate;

    private String placeholders;

}
