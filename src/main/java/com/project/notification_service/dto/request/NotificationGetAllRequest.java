package com.project.notification_service.dto.request;

import com.project.notification_service.model.enums.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationGetAllRequest {

    private Integer page;
    private Integer size;
    private NotificationStatus status;
}
