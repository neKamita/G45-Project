package uz.pdp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uz.pdp.entity.Door;
import uz.pdp.payload.EntityResponse;
import uz.pdp.repository.DoorRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for DoorService
 */
class DoorServiceTest {

    @Mock
    private DoorRepository doorRepository;

    @InjectMocks
    private DoorService doorService;

    private Door testDoor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test door
        testDoor = new Door();
        testDoor.setId(1L);
        testDoor.setName("Test Door");
        testDoor.setWidth(80.0);
        testDoor.setHeight(200.0);
        testDoor.setAvailable(true);
        testDoor.setActive(true);
    }

    @Test
    void getAllDoors_Success() {
        // Arrange
        List<Door> doorList = Arrays.asList(testDoor);
        when(doorRepository.findAll()).thenReturn(doorList);

        // Act
        List<Door> result = doorService.getAllDoors();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDoor.getId(), result.get(0).getId());
        verify(doorRepository).findAll();
    }

    @Test
    void getDoorById_Success() {
        // Arrange
        when(doorRepository.findById(1L)).thenReturn(Optional.of(testDoor));

        // Act
        Door result = doorService.getDoorById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testDoor.getId(), result.getId());
        assertEquals(testDoor.getName(), result.getName());
        verify(doorRepository).findById(1L);
    }

    @Test
    void createDoor_Success() {
        // Arrange
        when(doorRepository.save(any(Door.class))).thenReturn(testDoor);

        // Act
        EntityResponse<Door> result = doorService.createDoor(testDoor);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(testDoor.getId(), result.getData().getId());
        verify(doorRepository).save(testDoor);
    }

    @Test
    void updateDoor_Success() {
        // Arrange
        when(doorRepository.findById(1L)).thenReturn(Optional.of(testDoor));
        when(doorRepository.save(any(Door.class))).thenReturn(testDoor);

        Door updateDoor = new Door();
        updateDoor.setName("Updated Door");
        updateDoor.setWidth(90.0);
        updateDoor.setHeight(210.0);

        // Act
        EntityResponse<Door> result = doorService.updateDoor(1L, updateDoor);

        // Assert
        assertTrue(result.isSuccess());
        verify(doorRepository).save(any(Door.class));
    }

    @Test
    void deleteDoor_Success() {
        // Arrange
        when(doorRepository.findById(1L)).thenReturn(Optional.of(testDoor));
        doNothing().when(doorRepository).deleteById(1L);

        // Act
        EntityResponse<Void> result = doorService.deleteDoor(1L);

        // Assert
        assertTrue(result.isSuccess());
        verify(doorRepository).deleteById(1L);
    }

    @Test
    void configureDoorDimensions_Success() {
        // Arrange
        when(doorRepository.findById(1L)).thenReturn(Optional.of(testDoor));
        when(doorRepository.save(any(Door.class))).thenReturn(testDoor);

        // Act
        EntityResponse<Door> result = doorService.configureDoorDimensions(1L, 90.0, 210.0);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(90.0, result.getData().getWidth());
        assertEquals(210.0, result.getData().getHeight());
        verify(doorRepository).save(any(Door.class));
    }

    @Test
    void searchDoors_Success() {
        // Arrange
        List<Door> doorList = Arrays.asList(testDoor);
        when(doorRepository.findByNameContainingIgnoreCase(anyString())).thenReturn(doorList);

        // Act
        List<Door> result = doorService.searchDoors("Test");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDoor.getName(), result.get(0).getName());
        verify(doorRepository).findByNameContainingIgnoreCase("Test");
    }

    @Test
    void updateDoorStatus_Success() {
        // Arrange
        when(doorRepository.findById(1L)).thenReturn(Optional.of(testDoor));
        when(doorRepository.save(any(Door.class))).thenReturn(testDoor);

        // Act
        EntityResponse<Door> result = doorService.updateDoorStatus(1L, false, false);

        // Assert
        assertTrue(result.isSuccess());
        assertFalse(result.getData().isAvailable());
        assertFalse(result.getData().isActive());
        verify(doorRepository).save(any(Door.class));
    }
}
