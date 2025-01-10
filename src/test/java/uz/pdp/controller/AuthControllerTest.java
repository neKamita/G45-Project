package uz.pdp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uz.pdp.dto.SignInRequest;
import uz.pdp.dto.SignUpRequest;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.AuthService;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("rawtypes")
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(authController)
            .setControllerAdvice(new ResponseEntityExceptionHandler() {})
            .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void signUp_Success() throws Exception {
        // Arrange
        SignUpRequest request = new SignUpRequest();
        request.setName("testUser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setLastname("Test");

        Map<String, String> serviceResponse = Map.of(
            "message", "User registered successfully",
            "token", "test-jwt-token"
        );

        when(authService.signUp(any(SignUpRequest.class)))
            .then((Answer<?>) ResponseEntity.ok(EntityResponse.success("User registered successfully", serviceResponse)));

        // Act & Assert
        mockMvc.perform(post("/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("User registered successfully"))
            .andExpect(jsonPath("$.data.token").value("test-jwt-token"));
    }

    @Test
    void signUp_Failure_InvalidRequest() throws Exception {
        // Arrange
        SignUpRequest request = new SignUpRequest();
        // Missing required fields

        Map<String, String> errorResponse = Map.of(
            "message", "Name is required"
        );

        when(authService.signUp(any(SignUpRequest.class)))
            .then((Answer<?>) ResponseEntity.badRequest().body(EntityResponse.error("Name is required", errorResponse)));

        // Act & Assert
        mockMvc.perform(post("/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Name is required"));
    }

    @Test
    void signIn_Success() throws Exception {
        // Arrange
        SignInRequest request = new SignInRequest();
        request.setName("testUser");
        request.setPassword("password123");

        Map<String, String> serviceResponse = Map.of(
            "token", "test-jwt-token"
        );

        when(authService.signIn(any(SignInRequest.class)))
            .then((Answer<?>) ResponseEntity.ok(EntityResponse.success("Successfully signed in", serviceResponse)));

        // Act & Assert
        mockMvc.perform(post("/api/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Successfully signed in"))
            .andExpect(jsonPath("$.data.token").value("test-jwt-token"));
    }

    @Test
    void signIn_Failure_InvalidCredentials() throws Exception {
        // Arrange
        SignInRequest request = new SignInRequest();
        request.setName("testUser");
        request.setPassword("wrongPassword");

        Map<String, String> errorResponse = Map.of(
            "message", "Invalid name or password"
        );

        when(authService.signIn(any(SignInRequest.class)))
            .then((Answer<?>) ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(EntityResponse.error("Invalid name or password", errorResponse)));

        // Act & Assert
        mockMvc.perform(post("/api/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Invalid name or password"));
    }
} 