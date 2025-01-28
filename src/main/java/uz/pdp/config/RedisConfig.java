package uz.pdp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 🚀 The Speed Force: Redis Configuration
 * 
 * Welcome to our high-performance caching layer!
 * Where data goes to take a quick vacation before returning to the database.
 * 
 * Technical Features:
 * - Standalone Redis setup (because cluster is for the brave)
 * - JSON serialization with Java 8 time support (because time waits for no door!)
 * - TTL management (because even cached data needs a retirement plan)
 * - Connection pooling (because sharing is caring)
 * 
 * Cache TTLs:
 * - Addresses: 1 hour (they don't move that often!)
 * - Map Points: 2 hours (our doors are pretty stationary)
 * - Search Results: 30 minutes (fresh but not too fresh)
 * 
 * Remember: Redis is like a good cup of coffee ☕
 * - Too little: Everything is slow
 * - Too much: Everything crashes
 * - Just right: Pure magic!
 *
 * @version 1.0
 * @since 2025-01-17
 */
@Configuration
@EnableCaching
public class RedisConfig {
    
    @Value("${spring.data.redis.host}")
    private String redisHost;
    
    @Value("${spring.data.redis.port}")
    private int redisPort;
    
    @Value("${spring.data.redis.password}")
    private String redisPassword;

    /**
     * Creates the mythical Redis connection factory.
     * This is where the magic begins! ✨
     * 
     * Technical Details:
     * - Sets up standalone Redis connection
     * - Configures authentication
     * - Establishes connection pooling
     * 
     * Pro tip: If this fails, check if Redis is actually running
     * before spending 3 hours debugging your code. We've all been there. 😅
     *
     * @return A shiny new Lettuce connection factory
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        if (!redisPassword.isEmpty()) {
            redisConfig.setPassword(redisPassword);
        }
        
        return new LettuceConnectionFactory(redisConfig);
    }
    
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
    
    /**
     * Crafts the legendary RedisTemplate.
     * The Swiss Army knife for all your Redis operations!
     * 
     * Features:
     * - JSON serialization with Java 8 time support (because time waits for no door!)
     * - String keys (because who needs more complexity?)
     * - Automatic type conversion (it's basically magic)
     * 
     * Note: If you're storing something weird and it breaks,
     * remember: Redis is like a refrigerator - it works best
     * when you don't try to store your car in it. 🚗
     *
     * @param connectionFactory The connection to our Redis sanctuary
     * @return A fully loaded RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use our custom ObjectMapper with Java 8 time support
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper());
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Creates a specialized RedisTemplate for String-Integer operations.
     * This template is optimized for numeric operations and counters.
     *
     * @param connectionFactory The Redis connection factory
     * @return A RedisTemplate specifically for String keys and Integer values
     */
    @Bean
    public RedisTemplate<String, Integer> stringIntegerRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Conjures the CacheManager of legends.
     * Because manually managing cache is so last century!
     * 
     * Configuration:
     * - TTL: 1 hour (because forever is a really long time)
     * - JSON serialization with Java 8 time support (because time waits for no door!)
     * - Null value handling (because sometimes nothing is something)
     * 
     * Remember: Cache invalidation is one of the hardest things in CS.
     * The other two are naming things and off-by-one errors. 😉
     *
     * @param connectionFactory Your ticket to Redis paradise
     * @return The keeper of the cache
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default configuration with Java 8 time support
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))  // Default TTL
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                new GenericJackson2JsonRedisSerializer(redisObjectMapper())
            ));
        
        // Custom configurations for different caches
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        
        // Address caches (1 hour TTL)
        cacheConfigs.put("addresses", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigs.put("address", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Map points cache (2 hours TTL)
        cacheConfigs.put("map-points", defaultConfig.entryTtl(Duration.ofHours(2)));
        
        // Build cache manager
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigs)
            .build();
    }
}