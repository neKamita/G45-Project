package uz.pdp.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {

    @Bean
    @ConditionalOnMissingBean(RedisConnectionFactory.class)
    public CacheManager cacheManager() {
        return new NoOpCacheManager();
    }

    @Bean
    @ConditionalOnMissingBean(RedisConnectionFactory.class)
    public RedisTemplate<String, Object> redisTemplate() {
        return new RedisTemplate<>();
    }
}