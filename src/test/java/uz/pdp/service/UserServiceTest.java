package uz.pdp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import uz.pdp.entity.EmailVerification;
import uz.pdp.entity.User;
import uz.pdp.enums.Role;
import uz.pdp.enums.VerificationType;
import uz.pdp.payload.EntityResponse;
import uz.pdp.repository.EmailVerificationRepository;
import uz.pdp.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for UserService
 */
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private RedisTemplate<String, Integer> redisTemplate;

    @Mock
    private ValueOperations<String, Integer> valueOperations;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private EmailVerification testVerification;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);

        // Setup test verification
        testVerification = new EmailVerification();
        testVerification.setId(1L);
        testVerification.setEmail("test@example.com");
        testVerification.setCode("123456");
        testVerification.setType(VerificationType.SELLER_REQUEST);
        testVerification.setExpiryTime(LocalDateTime.now().plusMinutes(15));

        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn("Test User");

        // Setup Redis template
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void getCurrentUser_Success() {
        // Arrange
        when(userRepository.findByName("Test User")).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getCurrentUser();

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getName(), result.getName());
        verify(userRepository).findByName("Test User");
    }

    @Test
    void getAllUsers_Success() {
        // Arrange
        List<User> userList = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(userList);

        // Act
        EntityResponse<List<User>> result = userService.getAllUsers();

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(1, result.getData().size());
        assertEquals(testUser.getId(), result.getData().get(0).getId());
        verify(userRepository).findAll();
    }

    @Test
    void requestSeller_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(emailService.sendSellerVerificationEmail(anyString(), anyString()))
            .thenReturn(EntityResponse.success("Email sent"));

        // Act
        EntityResponse<User> result = userService.requestSeller(1L);

        // Assert
        assertTrue(result.isSuccess());
        assertTrue(result.getData().isSellerRequestPending());
        verify(userRepository).save(any(User.class));
        verify(emailService).sendSellerVerificationEmail(anyString(), anyString());
    }

    @Test
    void verifySellerEmail_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(emailVerificationRepository.findByUserIdAndVerificationCodeAndTypeAndVerifiedFalseAndExpiryTimeAfter(
            eq(1L), anyString(), eq(VerificationType.SELLER_REQUEST), any(LocalDateTime.class)))
            .thenReturn(Optional.of(testVerification));

        // Act
        EntityResponse<User> result = userService.verifySellerEmail(1L, "123456");

        // Assert
        assertTrue(result.isSuccess());
        verify(emailVerificationRepository).save(any(EmailVerification.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void verifySellerEmail_InvalidCode() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(emailVerificationRepository.findByUserIdAndVerificationCodeAndTypeAndVerifiedFalseAndExpiryTimeAfter(
            eq(1L), anyString(), eq(VerificationType.SELLER_REQUEST), any(LocalDateTime.class)))
            .thenReturn(Optional.empty());

        // Act
        EntityResponse<User> result = userService.verifySellerEmail(1L, "invalid");

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Invalid or expired verification code", result.getMessage());
    }

    @Test
    void sendVerificationEmail_Success() {
        // Arrange
        when(emailService.sendSellerVerificationEmail(anyString(), anyString()))
            .thenReturn(EntityResponse.success("Email sent"));

        // Act
        EntityResponse<Void> result = userService.sendVerificationEmail("test@example.com", VerificationType.SELLER_REQUEST);

        // Assert
        assertTrue(result.isSuccess());
        verify(emailVerificationRepository).save(any(EmailVerification.class));
        verify(emailService).sendSellerVerificationEmail(anyString(), anyString());
    }
}
