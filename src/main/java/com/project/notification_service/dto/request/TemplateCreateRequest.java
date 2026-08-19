package com.project.notification_service.dto.request;

import com.project.notification_service.model.enums.Channel;
import com.project.notification_service.model.enums.EventType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TemplateCreateRequest {

    @NotNull(message = "Event type is required")
    private EventType eventType;

    @NotNull(message = "Channel is required")
    private Channel channel;

    @NotNull(message = "Subject template is required")
    private String subjectTemplate;

    private MultipartFile bodyTemplate;

    @AssertTrue(message = "Body template is required for EMAIl channel")
    public boolean hasEmailChannelBodyTemplate() {
        return channel == Channel.EMAIL && bodyTemplate != null;
    }
}
