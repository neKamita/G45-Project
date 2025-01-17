package uz.pdp.controller.graphql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uz.pdp.entity.Door;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.DoorService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for DoorGraphQLController
 */
class DoorGraphQLControllerTest {

    @Mock
    private DoorService doorService;

    @InjectMocks
    private DoorGraphQLController doorGraphQLController;

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
    }

    @Test
    void getAllDoors_ReturnsAllDoors() {
        // Arrange
        List<Door> doorList = Arrays.asList(testDoor);
        when(doorService.getAllDoors()).thenReturn(doorList);

        // Act
        List<Door> result = doorGraphQLController.getAllDoors();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDoor.getId(), result.get(0).getId());
        verify(doorService).getAllDoors();
    }

    @Test
    void getDoorById_ReturnsDoor() {
        // Arrange
        when(doorService.getDoorById(1L)).thenReturn(testDoor);

        // Act
        Door result = doorGraphQLController.getDoorById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testDoor.getId(), result.getId());
        assertEquals(testDoor.getName(), result.getName());
        verify(doorService).getDoorById(1L);
    }

    @Test
    void searchDoors_ReturnsMatchingDoors() {
        // Arrange
        List<Door> doorList = Arrays.asList(testDoor);
        when(doorService.searchDoors("Test")).thenReturn(doorList);

        // Act
        List<Door> result = doorGraphQLController.searchDoors("Test");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDoor.getName(), result.get(0).getName());
        verify(doorService).searchDoors("Test");
    }

    @Test
    void createDoor_Success() {
        // Arrange
        when(doorService.createDoor(any(Door.class))).thenReturn(EntityResponse.success("Door created", testDoor));

        // Act
        Door result = doorGraphQLController.createDoor(testDoor);

        // Assert
        assertNotNull(result);
        assertEquals(testDoor.getId(), result.getId());
        verify(doorService).createDoor(testDoor);
    }

    @Test
    void updateDoor_Success() {
        // Arrange
        when(doorService.updateDoor(eq(1L), any(Door.class))).thenReturn(EntityResponse.success("Door updated", testDoor));

        // Act
        Door result = doorGraphQLController.updateDoor(1L, testDoor);

        // Assert
        assertNotNull(result);
        assertEquals(testDoor.getId(), result.getId());
        verify(doorService).updateDoor(1L, testDoor);
    }

    @Test
    void configureDoorDimensions_Success() {
        // Arrange
        Door configuredDoor = new Door();
        configuredDoor.setId(1L);
        configuredDoor.setWidth(90.0);
        configuredDoor.setHeight(210.0);
        when(doorService.configureDoorDimensions(1L, 90.0, 210.0))
            .thenReturn(EntityResponse.success("Dimensions configured", configuredDoor));

        // Act
        Door result = doorGraphQLController.configureDoorDimensions(1L, 90.0, 210.0);

        // Assert
        assertNotNull(result);
        assertEquals(90.0, result.getWidth());
        assertEquals(210.0, result.getHeight());
        verify(doorService).configureDoorDimensions(1L, 90.0, 210.0);
    }
}
