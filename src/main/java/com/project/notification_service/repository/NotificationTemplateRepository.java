package com.project.notification_service.repository;

import com.project.notification_service.model.NotificationTemplate;
import com.project.notification_service.model.enums.Channel;
import com.project.notification_service.model.enums.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long>, JpaSpecificationExecutor<NotificationTemplate> {

    Optional<NotificationTemplate> findByChannelAndEventType(Channel channel, EventType eventType);
}
