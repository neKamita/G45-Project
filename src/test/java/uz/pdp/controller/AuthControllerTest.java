package uz.pdp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
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
class AuthControllerTest {

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
            .build();  
        objectMapper = new ObjectMapper();
    }

    @Test
    void signUp_Success() throws Exception {
        // Arrange
        SignUpRequest request = new SignUpRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setLastname("Test"); 

        when(authService.signUp(any(SignUpRequest.class)))
            .thenReturn(ResponseEntity.ok(
                EntityResponse.success("User registered successfully", 
                    Map.of("message", "User registered successfully"))
            ));

        // Act & Assert
        mockMvc.perform(post("/api/auth/sign-up") // Correct URL
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("User registered successfully"))
            .andExpect(jsonPath("$.data.message").value("User registered successfully"));
    }

    @Test
    void signIn_Success() throws Exception {
        // Arrange
        SignInRequest request = new SignInRequest();
        request.setName("test");
        request.setPassword("password123");

        when(authService.signIn(any(SignInRequest.class)))
            .thenReturn(ResponseEntity.ok(
                EntityResponse.success("Login successful",
                    Map.of("token", "test-token"))
            ));

        // Act & Assert
        mockMvc.perform(post("/api/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void signIn_Failure_InvalidCredentials() throws Exception {
        // Arrange
        SignInRequest request = new SignInRequest();
        request.setName("test");
        request.setPassword("wrong-password");

        when(authService.signIn(any(SignInRequest.class)))
            .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(EntityResponse.error("Invalid credentials", null)));

        // Act & Assert
        mockMvc.perform(post("/api/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void signUp_Failure_DuplicateEmail() throws Exception {
        // Arrange
        SignUpRequest request = new SignUpRequest();
        request.setName("Test User");
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setLastname("Test");

        when(authService.signUp(any(SignUpRequest.class)))
            .thenReturn(ResponseEntity.badRequest()
                .body(EntityResponse.error("Email is already taken")));

        // Act & Assert
        mockMvc.perform(post("/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Email is already taken"))
            .andDo(result -> System.out.println(result.getResponse().getContentAsString()));
    }

    @Test
    void signUp_Failure_InvalidPassword() throws Exception {
        // Arrange
        SignUpRequest request = new SignUpRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("123"); // Too short
        request.setLastname("Test");

        // Act & Assert
        mockMvc.perform(post("/api/auth/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andDo(result -> System.out.println(result.getResponse().getContentAsString()));
    }

    @Test
    void signIn_Failure_InvalidPassword() throws Exception {
        // Arrange
        SignInRequest request = new SignInRequest();
        request.setName("test");
        request.setPassword("wrongpassword");

        when(authService.signIn(any(SignInRequest.class)))
            .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(EntityResponse.error("Invalid name or password")));

        // Act & Assert
        mockMvc.perform(post("/api/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Invalid name or password"));
    }

    @Test
    void signIn_Failure_MissingCredentials() throws Exception {
        // Arrange
        SignInRequest request = new SignInRequest();
        // Missing credentials

        // Act & Assert
        mockMvc.perform(post("/api/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}