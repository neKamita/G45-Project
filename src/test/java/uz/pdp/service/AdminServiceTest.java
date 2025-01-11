package uz.pdp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uz.pdp.dto.SellerRequestDto;
import uz.pdp.entity.User;
import uz.pdp.enums.Role;
import uz.pdp.payload.EntityResponse;
import uz.pdp.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AdminService adminService;

    private SellerRequestDto sellerRequestDto;
    private User testUser;

    @BeforeEach
    void setUp() {
        sellerRequestDto = new SellerRequestDto();
        sellerRequestDto.setUserId(1L);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setRole(Role.USER);
        testUser.setSellerRequestPending(true);
    }

    @Test
    void approveSeller_Success() {
        // Arrange
        when(userRepository.findById(sellerRequestDto.getUserId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(emailService.sendVerificationEmail(anyString()))
            .thenReturn(EntityResponse.success("Email sent"));

        // Act
        boolean result = adminService.approveSeller(sellerRequestDto);

        // Assert
        assertTrue(result);
        assertEquals(Role.SELLER, testUser.getRole());
        assertFalse(testUser.isSellerRequestPending());
        verify(userRepository).findById(sellerRequestDto.getUserId());
        verify(userRepository).save(testUser);
        verify(emailService).sendVerificationEmail(testUser.getEmail());
    }

    @Test
    void approveSeller_UserNotFound() {
        // Arrange
        when(userRepository.findById(sellerRequestDto.getUserId())).thenReturn(Optional.empty());

        // Act
        boolean result = adminService.approveSeller(sellerRequestDto);

        // Assert
        assertFalse(result);
        verify(userRepository).findById(sellerRequestDto.getUserId());
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(anyString());
    }

    @Test
    void approveSeller_EmailServiceFailure() {
        // Arrange
        when(userRepository.findById(sellerRequestDto.getUserId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(emailService.sendVerificationEmail(anyString()))
            .thenReturn(EntityResponse.error("Failed to send email"));

        // Act
        boolean result = adminService.approveSeller(sellerRequestDto);

        // Assert
        assertTrue(result); // Should still succeed even if email fails
        assertEquals(Role.SELLER, testUser.getRole());
        assertFalse(testUser.isSellerRequestPending());
        verify(userRepository).save(testUser);
        verify(emailService).sendVerificationEmail(testUser.getEmail());
    }

    @Test
    void approveSeller_DatabaseError() {
        // Arrange
        when(userRepository.findById(sellerRequestDto.getUserId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // Act
        boolean result = adminService.approveSeller(sellerRequestDto);

        // Assert
        assertFalse(result);
        verify(userRepository).findById(sellerRequestDto.getUserId());
        verify(userRepository).save(testUser);
        verify(emailService, never()).sendVerificationEmail(anyString());
    }
}
