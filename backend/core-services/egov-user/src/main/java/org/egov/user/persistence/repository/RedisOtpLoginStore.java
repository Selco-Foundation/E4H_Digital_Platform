package org.egov.user.persistence.repository;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.egov.user.domain.service.otplogin.OtpLoginStore;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RedisOtpLoginStore implements OtpLoginStore {

    private final StringRedisTemplate redisTemplate;

    public RedisOtpLoginStore(RedisConnectionFactory connectionFactory) {
        this.redisTemplate = new StringRedisTemplate(connectionFactory);
    }

    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void put(String key, String value, long ttlSeconds) {
        redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean putIfAbsent(String key, String value, long ttlSeconds) {
        Boolean created = redisTemplate.opsForValue().setIfAbsent(key, value);
        if (Boolean.TRUE.equals(created)) {
            redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
            return true;
        }
        return false;
    }

    @Override
    public long increment(String key, long ttlSeconds) {
        Long value = redisTemplate.opsForValue().increment(key, 1L);
        if (value != null && value == 1L) {
            redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        }
        return value == null ? 0L : value;
    }

    @Override
    public boolean delete(String key) {
        // RedisTemplate.delete is void in spring-data-redis 1.8; DEL tells us whether this call removed it.
        Long removed = redisTemplate.execute((RedisCallback<Long>) connection ->
                connection.del(key.getBytes(StandardCharsets.UTF_8)));
        return removed != null && removed > 0;
    }
}
