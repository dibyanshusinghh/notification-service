package com.project.notification_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.notification_service.dto.cache.NotificationPreferenceCacheDto;
import com.project.notification_service.dto.cache.NotificationTemplateCacheDto;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisTemplateConfig {

    private static <T> RedisSerializer<T> typedJsonSerializer(Class<T> type) {
        ObjectMapper mapper = new ObjectMapper();
        return new RedisSerializer<T>() {
            @Override
            public byte[] serialize(T value) throws SerializationException {
                try {
                    return mapper.writeValueAsBytes(value);
                } catch (Exception e) {
                    throw new SerializationException("Serialization failed", e);
                }
            }

            @Override
            public T deserialize(byte[] bytes) throws SerializationException {
                if (bytes == null || bytes.length == 0) return null;
                try {
                    return mapper.readValue(bytes, type);
                } catch (Exception e) {
                    throw new SerializationException("Deserialization failed", e);
                }
            }
        };
    }

    @Bean
    public RedisTemplate<String, NotificationPreferenceCacheDto> redisTemplateForPreference(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, NotificationPreferenceCacheDto> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(typedJsonSerializer(NotificationPreferenceCacheDto.class));
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public RedisTemplate<String, NotificationTemplateCacheDto> redisTemplateForTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, NotificationTemplateCacheDto> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(typedJsonSerializer(NotificationTemplateCacheDto.class));
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public RedisTemplate<String, String> dedupRedisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}
