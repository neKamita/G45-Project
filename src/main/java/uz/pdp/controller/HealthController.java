package uz.pdp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.v3.oas.annotations.tags.Tag;
import uz.pdp.payload.EntityResponse;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health Check", description = "APIs for system health monitoring")
public class HealthController {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    
    @GetMapping("/redis")
    @Operation(summary = "Check Redis connection status")
    public ResponseEntity<EntityResponse<String>> checkRedisHealth() {
        try {
            redisConnectionFactory.getConnection().ping();
            logger.info("Redis health check passed");
            return ResponseEntity.ok(EntityResponse.success("Redis is healthy"));
        } catch (Exception e) {
            logger.error("Redis health check failed: {}", e.getMessage());
            return ResponseEntity.status(503)
                .body(EntityResponse.error("Redis is down: " + e.getMessage()));
        }
    }
}