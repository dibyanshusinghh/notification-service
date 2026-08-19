package com.project.notification_service.model;

import com.project.notification_service.model.enums.Channel;
import com.project.notification_service.model.enums.EventType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "user")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "notification_preferences",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "channel", "event_type"}))
public class NotificationPreference {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "enabled", nullable = false)
    @ColumnDefault(value = "true")
    private Boolean enabled;

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
