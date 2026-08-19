package com.project.notification_service.dto.request;

import com.project.notification_service.model.enums.Channel;
import com.project.notification_service.model.enums.EventType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PreferenceUpdateRequest {

    private Long id;

    private Channel channel;
    private EventType eventType;
    private Boolean enabled;

    @AssertTrue(message = "At least one field must be provided")
    public boolean hasAnyField() {
        return channel != null
                || eventType != null
                || enabled != null;
    }

}

