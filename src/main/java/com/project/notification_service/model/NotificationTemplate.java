package com.project.notification_service.model;

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
@Entity
@Table(name = "notification_templates",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_type", "channel"}))
public class NotificationTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private Channel channel;

    @Column(name = "subject_template", nullable = true, length = 255)
    private String subjectTemplate;

    @Column(name = "body_template", nullable = true, columnDefinition = "TEXT")
    private String bodyTemplate;

    @Column(name = "placeholders", nullable = true, columnDefinition = "TEXT")
    private String placeholders;

    @Column(name = "created_at", nullable = false)
    @ColumnDefault(value = "CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = true)
    private Long createdBy;

    @Column(name = "updated_at", nullable = false)
    @ColumnDefault(value = "CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", nullable = true)
    private Long updatedBy;

}
