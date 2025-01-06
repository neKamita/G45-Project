package uz.pdp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.persistence.EntityNotFoundException;
import uz.pdp.entity.Door;
import uz.pdp.enums.Color;
import uz.pdp.enums.Size;
import uz.pdp.repository.DoorRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DoorServiceTest {

    @Mock
    private DoorRepository doorRepository;

    @InjectMocks
    private DoorService doorService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testConfigureDoor_Success() {
        Door door = new Door();
        door.setId(1L);
        door.setSize(Size.STANDARD);
        door.setColor(Color.WHITE);

        when(doorRepository.findById(1L)).thenReturn(Optional.of(door));
        when(doorRepository.save(any(Door.class))).thenReturn(door);

        Door configuredDoor = doorService.configureDoor(1L, Size.CUSTOM, Color.BLACK, 250.0, 220.0);

        assertEquals(Size.CUSTOM, configuredDoor.getSize());
        assertEquals(Color.BLACK, configuredDoor.getColor());
        assertEquals(250.0, configuredDoor.getCustomWidth());
        assertEquals(220.0, configuredDoor.getCustomHeight());
    }

    @Test
    public void testConfigureDoor_NotFound() {
        when(doorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> doorService.configureDoor(1L, Size.CUSTOM, Color.BLACK, 250.0, 220.0));
    }

    @Test
    public void testGetDoor_Success() {
        Door door = new Door();
        door.setId(1L);
        door.setName("Test Door");

        when(doorRepository.findById(1L)).thenReturn(Optional.of(door));

        Door foundDoor = doorService.getDoor(1L);

        assertEquals(1L, foundDoor.getId());
        assertEquals("Test Door", foundDoor.getName());
    }

    @Test
    public void testGetDoor_NotFound() {
        when(doorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> doorService.getDoor(1L));
    }

    @Test
    public void testGetAllDoors_Success() {
        Door door1 = new Door();
        door1.setId(1L);
        door1.setName("Door 1");

        Door door2 = new Door();
        door2.setId(2L);
        door2.setName("Door 2");

        when(doorRepository.findAll()).thenReturn(Arrays.asList(door1, door2));

        List<Door> doors = doorService.getAllDoors();

        assertEquals(2, doors.size());
        assertEquals("Door 1", doors.get(0).getName());
        assertEquals("Door 2", doors.get(1).getName());
    }

    @Test
    public void testCreateDoor_Success() {
        Door door = new Door();
        door.setName("New Door");

        when(doorRepository.save(any(Door.class))).thenReturn(door);

        Door createdDoor = doorService.createDoor(door);

        assertEquals("New Door", createdDoor.getName());
    }

    @Test
    public void testUpdateDoor_Success() {
        Door existingDoor = new Door();
        existingDoor.setId(1L);
        existingDoor.setName("Existing Door");

        Door updatedDoor = new Door();
        updatedDoor.setName("Updated Door");

        when(doorRepository.findById(1L)).thenReturn(Optional.of(existingDoor));
        when(doorRepository.save(any(Door.class))).thenReturn(updatedDoor);

        Door result = doorService.updateDoor(1L, updatedDoor);

        assertEquals("Updated Door", result.getName());
    }

    @Test
    public void testUpdateDoor_NotFound() {
        Door updatedDoor = new Door();
        updatedDoor.setName("Updated Door");

        when(doorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> doorService.updateDoor(1L, updatedDoor));
    }

    @Test
    public void testDeleteDoor_Success() {
        Door door = new Door();
        door.setId(1L);

        when(doorRepository.findById(1L)).thenReturn(Optional.of(door));
        doNothing().when(doorRepository).delete(door);

        doorService.deleteDoor(1L);

        verify(doorRepository, times(1)).delete(door);
    }

    @Test
    public void testDeleteDoor_NotFound() {
        when(doorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> doorService.deleteDoor(1L));
    }
}