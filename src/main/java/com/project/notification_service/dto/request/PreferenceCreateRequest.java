package com.project.notification_service.dto.request;

import com.project.notification_service.model.enums.Channel;
import com.project.notification_service.model.enums.EventType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.bind.DefaultValue;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreferenceCreateRequest {

    @NotNull(message = "Channel is required")
    private Channel channel;

    @NotNull(message = "Event type is required")
    private EventType eventType;

    private Boolean enabled;

}
