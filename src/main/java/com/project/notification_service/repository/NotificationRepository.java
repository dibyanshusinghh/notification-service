package com.project.notification_service.repository;

import com.project.notification_service.model.Notification;
import com.project.notification_service.model.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {
    Page<Notification> findByUserIdAndStatus(Long id, NotificationStatus notificationStatus, Pageable pageable);
}
