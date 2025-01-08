package uz.pdp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;
    
    @GetMapping("/redis")
    public ResponseEntity<String> checkRedisHealth() {
        try {
            redisConnectionFactory.getConnection().ping();
            return ResponseEntity.ok("Redis is healthy");
        } catch (Exception e) {
            return ResponseEntity.status(503).body("Redis is down");
        }
    }
} 