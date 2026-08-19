package com.project.notification_service.dto.response;

import com.project.notification_service.model.User;
import com.project.notification_service.model.enums.Channel;
import com.project.notification_service.model.enums.EventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PreferenceGetResponse {

    private Long id;
    private Long user_id;
    private Channel channel;
    private EventType eventType;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
