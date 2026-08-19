package com.project.notification_service.dto.request;

import com.project.notification_service.model.enums.Channel;
import com.project.notification_service.model.enums.EventType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationAddRequest {

    private Long id;

    private String email;

    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    @NotNull(message = "Channel is required")
    private Channel channel;

    @NotNull(message = "Event type is required")
    private EventType eventType;

    @NotNull(message = "Payload is required")
    private Map<String, Object> payload;

    @NotNull(message = "Idempotency key is required")
    private String idempotencyKey;

}
