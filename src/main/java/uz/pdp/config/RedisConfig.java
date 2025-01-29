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
//@EnableCaching  // Temporarily disabled
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
     * Mock Redis connection factory.
     * Our pretend connection to nowhere! 
     * 
     * @return A mock connection factory that doesn't actually connect
     */
    @Bean
    public RedisConnectionFactory connectionFactory() {
        // Using LettuceConnectionFactory with default settings
        // This won't actually connect to Redis
        return new LettuceConnectionFactory();
    }

    /**
     * Mock RedisTemplate for String-Integer operations.
     * This is our "pretend" Redis implementation! 
     * 
     * @return A mock RedisTemplate that doesn't actually store data
     */
    @Bean
    public RedisTemplate<String, Integer> redisTemplate() {
        RedisTemplate<String, Integer> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Mock RedisTemplate for Object storage.
     * Another member of our pretend Redis family! 
     * 
     * @return A mock RedisTemplate that doesn't actually store data
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplateObject() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}