package com.project.notification_service.service;

import com.project.notification_service.dto.cache.NotificationPreferenceCacheDto;
import com.project.notification_service.dto.request.NotificationAddRequest;
import com.project.notification_service.exception.EmailSendingException;
import com.project.notification_service.exception.ResourceNotFoundException;
import com.project.notification_service.model.Notification;
import com.project.notification_service.model.NotificationPreference;
import com.project.notification_service.model.enums.Channel;
import com.project.notification_service.model.enums.NotificationStatus;
import com.project.notification_service.repository.NotificationPreferenceRepository;
import com.project.notification_service.repository.NotificationRepository;
import com.project.notification_service.utility.EmailSenderUtil;
import com.project.notification_service.utility.EmailTemplateUtil;
import com.project.notification_service.utility.RenderedTemplate;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

import static java.lang.System.out;

@Service
public class NotificationDispatcher {

    private final RedisTemplate<String, NotificationPreferenceCacheDto> redisTemplateForPreference;
    private final RedisTemplate<String, String> dedupRedisTemplate;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final EmailTemplateUtil emailTemplateUtil;
    private final EmailSenderUtil emailSenderUtil;
    private final NotificationRepository notificationRepository;

    @Value("${notification.max-retry-count}")
    private int MAX_RETRY_COUNT;

    public NotificationDispatcher(
            @Qualifier("redisTemplateForPreference")
            RedisTemplate<String, NotificationPreferenceCacheDto> redisTemplateForPreference,
            @Qualifier("dedupRedisTemplate") RedisTemplate<String, String> dedupRedisTemplate,
            NotificationPreferenceRepository notificationPreferenceRepository, EmailTemplateUtil emailTemplateUtil, EmailSenderUtil emailSenderUtil, NotificationRepository notificationRepository) {
        this.redisTemplateForPreference = redisTemplateForPreference;
        this.dedupRedisTemplate = dedupRedisTemplate;
        this.notificationPreferenceRepository = notificationPreferenceRepository;
        this.emailTemplateUtil = emailTemplateUtil;
        this.emailSenderUtil = emailSenderUtil;
        this.notificationRepository = notificationRepository;
    }

    @RetryableTopic(
            attempts = "3",
            backOff = @BackOff(delay = 60000, multiplier = 2.0),
            dltStrategy = DltStrategy.NO_DLT
    )
    @KafkaListener(topics = "notification-events", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(NotificationAddRequest notificationAddRequest, Acknowledgment ack) {
        // Process the notification event message
        System.out.println("Received notification event: " + notificationAddRequest);

        // Check Redis If the key already exists (dedup)
        String redisKey = "dedup:" + notificationAddRequest.getIdempotencyKey() + ":" + notificationAddRequest.getChannel() + ":" + notificationAddRequest.getEventType();
        Boolean exists = dedupRedisTemplate.hasKey(redisKey);

        if (Boolean.TRUE.equals(exists)) {
            ack.acknowledge();
            return;
        }

        // Insert key if this key doesn't exist in the Redis
        Boolean inserted = dedupRedisTemplate.opsForValue().setIfAbsent(
                redisKey,
                "PROCESSED",
                Duration.ofSeconds(60));

        if (Boolean.FALSE.equals(inserted)) {
            ack.acknowledge();
            return;
        }

        // Fetch user preferences from Redis, if not present, fetch from DB and insert in Redis
        String redisKeyPrefs = "prefs:" + notificationAddRequest.getRecipientId() + ":" + notificationAddRequest.getChannel() + ":" + notificationAddRequest.getEventType();

        NotificationPreferenceCacheDto prefCacheDto =
                    redisTemplateForPreference
                        .opsForValue()
                        .get(redisKeyPrefs);

        if (prefCacheDto == null) {
            NotificationPreference notificationPreference = notificationPreferenceRepository.findByUserIdAndChannelAndEventType(notificationAddRequest.getRecipientId(),  notificationAddRequest.getChannel(), notificationAddRequest.getEventType());

            prefCacheDto =
                    NotificationPreferenceCacheDto.builder()
                            .id(notificationPreference.getId())
                            .userId(notificationPreference.getUser().getId())
                            .channel(notificationPreference.getChannel())
                            .eventType(notificationPreference.getEventType())
                            .enabled(notificationPreference.getEnabled())
                            .build();

            redisTemplateForPreference.opsForValue().set(
                    redisKeyPrefs,
                    prefCacheDto,
                    Duration.ofMinutes(10));
        }

        if (notificationAddRequest.getChannel() == Channel.EMAIL && prefCacheDto.getEnabled() == true) {
            // fetch notification data
            Notification notification = notificationRepository.findById(notificationAddRequest.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

            // Replace placeholders
            RenderedTemplate renderedTemplate = emailTemplateUtil.renderTemplate(notificationAddRequest.getChannel(), notificationAddRequest.getEventType(), notificationAddRequest.getPayload());

            try {
                // Send email
                emailSenderUtil.sendEmail(renderedTemplate.subject(), renderedTemplate.htmlBody(), notificationAddRequest.getEmail());

                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                notificationRepository.save(notification);
                ack.acknowledge();
            } catch (EmailSendingException ex) {
                notification.setAttemptCount(notification.getAttemptCount() + 1);
                notification.setErrorMessage(ex.getMessage());
                notification.setSentAt(LocalDateTime.now());

                if (notification.getAttemptCount() >= MAX_RETRY_COUNT) {
                    notification.setStatus(NotificationStatus.FAILED);
                    notificationRepository.save(notification);
                    ack.acknowledge();
                    return;
                }

                notificationRepository.save(notification);
                throw ex;
            }
        }


    }
}
