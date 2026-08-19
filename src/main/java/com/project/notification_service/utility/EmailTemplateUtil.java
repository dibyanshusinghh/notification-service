package com.project.notification_service.utility;

import com.project.notification_service.dto.cache.NotificationTemplateCacheDto;
import com.project.notification_service.exception.BadRequestException;
import com.project.notification_service.exception.IOException;
import com.project.notification_service.exception.ResourceNotFoundException;
import com.project.notification_service.model.NotificationTemplate;
import com.project.notification_service.model.enums.Channel;
import com.project.notification_service.model.enums.EventType;
import com.project.notification_service.repository.NotificationTemplateRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class EmailTemplateUtil {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(.*?)\\}\\}");

    private final NotificationTemplateRepository templateRepository;
    private final ObjectMapper objectMapper;

    private final RedisTemplate<String, NotificationTemplateCacheDto> redisTemplateForTemplate;

    public EmailTemplateUtil(NotificationTemplateRepository templateRepository, ObjectMapper objectMapper,
                             @Qualifier("redisTemplateForTemplate")
                             RedisTemplate<String, NotificationTemplateCacheDto> redisTemplateForTemplate) {
        this.templateRepository = templateRepository;
        this.objectMapper = objectMapper;
        this.redisTemplateForTemplate = redisTemplateForTemplate;
    }

    public RenderedTemplate renderTemplate(Channel channel, EventType eventType, Map<String, Object> templateData) {
        String redisKeyTemps = "temps:" + channel + ":" + eventType;

        NotificationTemplateCacheDto notificationTemplateCacheDto =
                 redisTemplateForTemplate
                        .opsForValue()
                        .get(redisKeyTemps);

        if (notificationTemplateCacheDto == null) {
            NotificationTemplate notificationTemplate = templateRepository.findByChannelAndEventType(channel, eventType)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Notification preference not found"));

            notificationTemplateCacheDto = NotificationTemplateCacheDto.builder()
                    .id(notificationTemplate.getId())
                    .channel(notificationTemplate.getChannel())
                    .eventType(notificationTemplate.getEventType())
                    .subjectTemplate(notificationTemplate.getSubjectTemplate())
                    .bodyTemplate(notificationTemplate.getBodyTemplate())
                    .placeholders(notificationTemplate.getPlaceholders())
                    .build();

            redisTemplateForTemplate.opsForValue().set(
                    redisKeyTemps,
                    notificationTemplateCacheDto,
                    Duration.ofMinutes(30));
        }

        String html = getHtmlTemplate(notificationTemplateCacheDto);
//        Map<String, Object> dataMap = parseTemplateData(templateData);
        String renderedHtml = replacePlaceholders(html, templateData);

        return new RenderedTemplate(notificationTemplateCacheDto.getSubjectTemplate(), renderedHtml);
    }

    public List<String> extractPlaceholders(String html) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(html);
        Set<String> placeholders = new LinkedHashSet<>();
        while (matcher.find()) {
            placeholders.add(matcher.group(1).trim());
        }
        return new ArrayList<>(placeholders);
    }

    private String getHtmlTemplate(NotificationTemplateCacheDto template) {
        String body = template.getBodyTemplate();
        if (body == null || body.isBlank()) {
            throw new IOException(
                    "HTML body template is empty or unavailable for channel=" +
                    template.getChannel() + ", eventType=" + template.getEventType());
        }
        return body;
    }

    private Map<String, Object> parseTemplateData(String templateData) {
        try {
            return objectMapper.readValue(templateData, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new BadRequestException("Failed to parse template_data JSON: " + e.getMessage());
        }
    }

    private String replacePlaceholders(String html, Map<String, Object> data) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(html);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            if (!data.containsKey(key)) {
                throw new BadRequestException(
                        "Placeholder '{{" + key + "}}' found in template but no corresponding value in template_data");
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(String.valueOf(data.get(key))));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
