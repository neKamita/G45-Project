package uz.pdp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import uz.pdp.entity.Door;
import uz.pdp.entity.User;
import uz.pdp.repository.DoorRepository;

@ExtendWith(MockitoExtension.class)
class DoorServiceTest {

    @Mock
    private DoorRepository doorRepository;

    @Mock
    private UserService userService;

    @Mock
    private DoorSecurityService doorSecurityService;

    @InjectMocks
    private DoorService doorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getDoor_Success() {
        // Arrange
        Long id = 1L;
        Door door = new Door();
        door.setId(id);
        when(doorRepository.findById(id)).thenReturn(Optional.of(door));

        // Act
        Door result = doorService.getDoor(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(doorRepository).findById(id);
    }

    @Test
    void getDoor_NotFound() {
        // Arrange
        Long id = 1L;
        when(doorRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> doorService.getDoor(id));
        verify(doorRepository).findById(id);
    }

    @Test
    void getAllDoors_Success() {
        // Arrange
        List<Door> doors = Arrays.asList(new Door(), new Door());
        when(doorRepository.findAll()).thenReturn(doors);

        // Act
        List<Door> result = doorService.getAllDoors();

        // Assert
        assertEquals(2, result.size());
        verify(doorRepository).findAll();
    }

    @Test
    void createDoor_Success() {
        // Arrange
        Door door = new Door();
        door.setPrice(100.0);
        door.setName("Test Door");
        User seller = new User();
        seller.setId(1L);
        
        when(userService.getCurrentUser()).thenReturn(seller);
        when(doorRepository.saveAndFlush(any(Door.class))).thenReturn(door);

        // Act
        Door result = doorService.createDoor(door);

        // Assert
        assertNotNull(result);
        assertEquals(seller, result.getSeller());
        assertEquals(100.0, result.getPrice());
        verify(doorRepository).saveAndFlush(door);
        verify(userService).getCurrentUser();
    }

    @Test
    void updateDoor_Success() {
        // Arrange
        Long id = 1L;
        Door existingDoor = new Door();
        existingDoor.setPrice(100.0);
        Door updatedDoor = new Door();
        updatedDoor.setName("Updated Name");
        updatedDoor.setPrice(150.0);
        
        when(doorRepository.findById(id)).thenReturn(Optional.of(existingDoor));
        when(doorRepository.save(any(Door.class))).thenReturn(existingDoor);

        // Act
        Door result = doorService.updateDoor(id, updatedDoor);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        verify(doorRepository).findById(id);
        verify(doorRepository).save(existingDoor);
    }

    @Test
    void createDoor_WithPrice_Success() {
        // Arrange
        Door door = new Door();
        door.setPrice(100.0); // Initialize price
        User seller = new User();
        when(userService.getCurrentUser()).thenReturn(seller);
        when(doorRepository.saveAndFlush(any(Door.class))).thenReturn(door);

        // Act
        Door result = doorService.createDoor(door);

        // Assert
        assertNotNull(result);
        assertEquals(seller, result.getSeller());
        verify(doorRepository).saveAndFlush(door);
    }
}