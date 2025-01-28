package uz.pdp.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jackson.JsonComponent;
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
import uz.pdp.payload.EntityResponse;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The Speed Force: Redis Configuration
 * 
 * Welcome to our high-performance caching layer!
 * Where data goes to take a quick vacation before returning to the database.
 * 
 * This configuration sets up:
 * - Redis connection (because network calls are so 2024)
 * - Cache settings (TTL, serialization, etc.)
 * - Custom serializers (because JSON should be clean)
 * 
 * Cache TTL Settings:
 * - User Data: 15 minutes
 * - Door States: 5 minutes
 * - Map Points: 2 hours (our doors are pretty stationary)
 * - Search Results: 30 minutes (fresh but not too fresh)
 * 
 * Remember: Redis is like a good cup of coffee 
 * - Too little: Everything is slow
 * - Too much: Everything crashes
 * - Just right: Pure magic!
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
     * Custom serializer for enums to output only the enum value.
     * Because who needs to know it's an enum? 
     */
    @JsonComponent
    public static class EnumSerializer extends JsonSerializer<Enum<?>> {
        @Override
        public void serialize(Enum<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (value != null) {
                gen.writeString(value.name());
            }
        }
    }

    /**
     * Custom serializer for collections to output just the list content.
     * Because sometimes less is more! 
     */
    @JsonComponent
    public static class CollectionSerializer extends JsonSerializer<Collection<?>> {
        @Override
        public void serialize(Collection<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (value != null) {
                gen.writeStartArray();
                for (Object item : value) {
                    if (item instanceof Enum<?>) {
                        gen.writeString(((Enum<?>) item).name());
                    } else {
                        provider.defaultSerializeValue(item, gen);
                    }
                }
                gen.writeEndArray();
            }
        }
    }

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        
        // Create a module for custom serializers
        SimpleModule module = new SimpleModule();
        module.addSerializer((Class<Enum<?>>) (Class<?>) Enum.class, new EnumSerializer());
        module.addSerializer((Class<Collection<?>>) (Class<?>) Collection.class, new CollectionSerializer());
        mapper.registerModule(module);
        
        // Configure to only include non-null values and ignore type info for most classes
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
        mapper.disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS);
        
        // Only include type info for EntityResponse
        mapper.setDefaultTyping(null); // Disable default typing
        mapper.addMixIn(EntityResponse.class, EntityResponseMixin.class);
        
        return mapper;
    }

    /**
     * Abstract class to preserve generic type information during serialization.
     * Because even doors need their type safety! 
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    abstract class EntityResponseMixin {
    }

    /**
     * Creates the mythical Redis connection factory.
     * This is where the magic begins! 
     * 
     * Technical Details:
     * - Sets up standalone Redis connection
     * - Configures authentication
     * - Establishes connection pooling
     * 
     * Pro tip: If this fails, check if Redis is actually running
     * before spending 3 hours debugging your code. We've all been there. 
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
     * when you don't try to store your car in it. 
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
     * The other two are naming things and off-by-one errors. 
     *
     * @param connectionFactory Your ticket to Redis paradise
     * @return The keeper of the cache
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Create a custom serializer with type information
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper());

        // Default configuration with Java 8 time support and custom serializer
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))  // Default TTL
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
            .disableCachingNullValues(); // Don't cache null values
        
        // Custom configurations for different caches
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        
        // Address caches (1 hour TTL)
        cacheConfigs.put("addresses", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigs.put("address", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // Map points cache (2 hours TTL)
        cacheConfigs.put("map-points", defaultConfig.entryTtl(Duration.ofHours(2)));
        
        // Build cache manager with custom configurations
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigs)
            .build();
    }
}