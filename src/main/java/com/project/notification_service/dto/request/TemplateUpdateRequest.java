package com.project.notification_service.dto.request;

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
public class TemplateUpdateRequest {

    @NotNull(message = "Id is required")
    private Long id;

    private String subjectTemplate;

    private MultipartFile bodyTemplate;

    @AssertTrue(message = "At least one field must be provided")
    public boolean hasAnyField() {
        return subjectTemplate != null
                || bodyTemplate != null;
    }
}
