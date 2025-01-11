package uz.pdp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import uz.pdp.config.filtr.JwtProvider;
import uz.pdp.dto.SignInRequest;
import uz.pdp.dto.SignUpRequest;
import uz.pdp.entity.User;
import uz.pdp.enums.Role;
import uz.pdp.payload.EntityResponse;
import uz.pdp.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthService authService;

    private SignUpRequest signUpRequest;
    private SignInRequest signInRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        signUpRequest = new SignUpRequest();
        signUpRequest.setName("testuser");
        signUpRequest.setEmail("test@example.com");
        signUpRequest.setPassword("password123");
        signUpRequest.setLastname("Test");

        signInRequest = new SignInRequest();
        signInRequest.setName("testuser");
        signInRequest.setPassword("password123");

        testUser = new User();
        testUser.setName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.USER);
    }

    @Test
    void signUp_Success() {
        // Arrange
        when(userRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtProvider.generateToken(anyString())).thenReturn("test-token");

        // Act
        ResponseEntity<EntityResponse<Map<String, String>>> response = 
            authService.signUp(signUpRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().success());
        assertEquals("User registered successfully", response.getBody().message());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signUp_DuplicateUsername() {
        // Arrange
        when(userRepository.findByName(signUpRequest.getName()))
            .thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<EntityResponse<Map<String, String>>> response = 
            authService.signUp(signUpRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals("Name is already taken", response.getBody().message());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signUp_DuplicateEmail() {
        // Arrange
        when(userRepository.findByName(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(signUpRequest.getEmail()))
            .thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<EntityResponse<Map<String, String>>> response = 
            authService.signUp(signUpRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals("Email is already taken", response.getBody().message());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signIn_Success() {
        // Arrange
        when(userRepository.findByName(signInRequest.getName()))
            .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(signInRequest.getPassword(), testUser.getPassword()))
            .thenReturn(true);
        when(jwtProvider.generateToken(anyString())).thenReturn("test-token");

        // Act
        ResponseEntity<EntityResponse<Map<String, String>>> response = 
            authService.signIn(signInRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().success());
        assertEquals("Successfully signed in", response.getBody().message());
        assertEquals("test-token", response.getBody().data().get("token"));
    }

    @Test
    void signIn_InvalidUsername() {
        // Arrange
        when(userRepository.findByName(signInRequest.getName()))
            .thenReturn(Optional.empty());

        // Act
        ResponseEntity<EntityResponse<Map<String, String>>> response = 
            authService.signIn(signInRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals("Invalid name or password", response.getBody().message());
    }

    @Test
    void signIn_InvalidPassword() {
        // Arrange
        when(userRepository.findByName(signInRequest.getName()))
            .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(signInRequest.getPassword(), testUser.getPassword()))
            .thenReturn(false);

        // Act
        ResponseEntity<EntityResponse<Map<String, String>>> response = 
            authService.signIn(signInRequest);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().success());
        assertEquals("Invalid name or password", response.getBody().message());
    }

    @Test
    void handleValidationErrors() {
        // Arrange
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("signUpRequest", "password", "Password is required");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        // Act
        EntityResponse<Map<String, String>> response = authService.handleValidationErrors(ex);

        // Assert
        assertFalse(response.success());
        assertEquals("Validation failed", response.message());
        assertEquals("Password is required", response.data().get("password"));
    }
}