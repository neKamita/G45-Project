package uz.pdp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import uz.pdp.entity.Door;
import uz.pdp.entity.User;
import uz.pdp.enums.Role;
import uz.pdp.repository.DoorRepository;

@ExtendWith(MockitoExtension.class)
class DoorSecurityServiceTest {

    @Mock
    private DoorRepository doorRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private DoorSecurityService doorSecurityService;

    private Door testDoor;
    private User testSeller;
    private User differentUser;

    @BeforeEach
    void setUp() {
        testSeller = new User();
        testSeller.setId(1L);
        testSeller.setRole(Role.SELLER);

        differentUser = new User();
        differentUser.setId(2L);
        differentUser.setRole(Role.SELLER);

        testDoor = new Door();
        testDoor.setId(1L);
        testDoor.setSeller(testSeller);
    }

    @Test
    void isSeller_Success_WhenUserIsSeller() {
        // Arrange
        when(doorRepository.findById(testDoor.getId())).thenReturn(Optional.of(testDoor));
        when(userService.getCurrentUser()).thenReturn(testSeller);

        // Act
        boolean result = doorSecurityService.isSeller(testDoor.getId());

        // Assert
        assertTrue(result);
        verify(doorRepository).findById(testDoor.getId());
        verify(userService).getCurrentUser();
    }

    @Test
    void isSeller_False_WhenUserIsNotSeller() {
        // Arrange
        when(doorRepository.findById(testDoor.getId())).thenReturn(Optional.of(testDoor));
        when(userService.getCurrentUser()).thenReturn(differentUser);

        // Act
        boolean result = doorSecurityService.isSeller(testDoor.getId());

        // Assert
        assertFalse(result);
        verify(doorRepository).findById(testDoor.getId());
        verify(userService).getCurrentUser();
    }

    @Test
    void isSeller_ThrowsException_WhenDoorNotFound() {
        // Arrange
        Long doorId = 999L;
        when(doorRepository.findById(doorId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
            () -> doorSecurityService.isSeller(doorId));
        assertEquals("Door not found", exception.getMessage());
        verify(doorRepository).findById(doorId);
        verify(userService, never()).getCurrentUser();
    }

    @Test
    void isSeller_ThrowsException_WhenDoorHasNoSeller() {
        // Arrange
        testDoor.setSeller(null);
        when(doorRepository.findById(testDoor.getId())).thenReturn(Optional.of(testDoor));
        when(userService.getCurrentUser()).thenReturn(testSeller);

        // Act & Assert
        NullPointerException exception = assertThrows(NullPointerException.class,
            () -> doorSecurityService.isSeller(testDoor.getId()));
        verify(doorRepository).findById(testDoor.getId());
        verify(userService).getCurrentUser();
    }
}
