package com.project.notification_service.model;

import com.project.notification_service.model.enums.Channel;
import com.project.notification_service.model.enums.EventType;
import com.project.notification_service.model.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "user")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "notifications", indexes = @Index(
        name = "idx_notifications_recipient",
        columnList = "recipient_id,created_at DESC"
))
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status;

    @Column(name = "attempt_count", nullable = false)
    @ColumnDefault("0")
    private int attemptCount;

    @Column(name = "error_message", nullable = true, columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "idempotency_key", nullable = true, length = 100, unique = true)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    @ColumnDefault(value = "CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "sent_at", nullable = true)
    private LocalDateTime sentAt;
}
