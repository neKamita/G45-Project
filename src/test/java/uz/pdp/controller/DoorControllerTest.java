package uz.pdp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasSize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.WebRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import uz.pdp.dto.DoorDto;
import uz.pdp.entity.Door;
import uz.pdp.enums.Color;
import uz.pdp.enums.Size;
import uz.pdp.mutations.DoorConfigInput;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.DoorService;

@ExtendWith(MockitoExtension.class)
class DoorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DoorService doorService;

    @InjectMocks 
    private DoorController doorController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(doorController)
            .setControllerAdvice(new RestExceptionHandler())
            .build();
        
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }
    
    @ControllerAdvice 
    class RestExceptionHandler extends ResponseEntityExceptionHandler {
        
        @ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex) {
            return new ResponseEntity<>(
                EntityResponse.error(ex.getMessage()),
                HttpStatus.NOT_FOUND
            );
        }
    }

    private Door createTestDoor(Long id) {
        Door door = new Door();
        door.setId(id);
        door.setName("Test Door");
        door.setDescription("Test Description");
        door.setPrice(100.0);
        door.setSize(Size.STANDARD);
        door.setColor(Color.WHITE);
        door.setFinalPrice(100.0);
        door.setImages(new ArrayList<>());
        door.setMaterial("Wood");
        door.setManufacturer("Test Manufacturer");
        door.setWarrantyYears(2);
        return door;
    }

    @Test
    void getDoor_Success() throws Exception {
        // Arrange
        Door door = createTestDoor(1L);
        when(doorService.getDoor(1L)).thenReturn(door);

        // Act & Assert
        mockMvc.perform(get("/api/doors/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("Test Door"));
    }

    @Test
    void getDoor_NotFound() throws Exception {
        when(doorService.getDoor(1L))
            .thenThrow(new EntityNotFoundException("Door not found"));

        mockMvc.perform(get("/api/doors/1"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getAllDoors_Success() throws Exception {
        List<Door> doors = Arrays.asList(createTestDoor(1L), createTestDoor(2L));
        when(doorService.getAllDoors()).thenReturn(doors);

        mockMvc.perform(get("/api/doors")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void createDoor_Success() throws Exception {
        // Arrange
        Door door = createTestDoor(null);
        Door savedDoor = createTestDoor(1L);
        when(doorService.createDoor(any(Door.class))).thenReturn(savedDoor);

        // Act & Assert
        mockMvc.perform(post("/api/doors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(door)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createDoor_ValidationError() throws Exception {
        DoorDto doorDto = new DoorDto(); // Empty for validation failure
        doorDto.setPrice(100.0); // Set minimal required fields to avoid NPE
        
        mockMvc.perform(post("/api/doors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doorDto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateDoor_Success() throws Exception {
        // Arrange
        Door door = createTestDoor(1L);
        when(doorService.updateDoor(eq(1L), any(Door.class))).thenReturn(door);

        // Act & Assert
        mockMvc.perform(put("/api/doors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(door)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void updateDoor_NotFound() throws Exception {
        when(doorService.updateDoor(eq(1L), any()))
            .thenThrow(new EntityNotFoundException("Door not found"));
    
        mockMvc.perform(put("/api/doors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createTestDoorDto())))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Door not found"))
            .andExpect(jsonPath("$.success").value(false));
    }
    

    @Test
    void deleteDoor_Success() throws Exception {
        // Arrange
        doNothing().when(doorService).deleteDoor(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/doors/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
    @Test
    void deleteDoor_NotFound() throws Exception {
        doThrow(new EntityNotFoundException("Door not found"))
            .when(doorService).deleteDoor(1L);
    
        mockMvc.perform(delete("/api/doors/1"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Door not found"))
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void configureDoor_Success() throws Exception {
        // Arrange
        Door door = createTestDoor(1L);
        DoorConfigInput configInput = new DoorConfigInput();
        configInput.setSize(Size.STANDARD);
        configInput.setColor(Color.WHITE);
        configInput.setWidth(100.0);
        configInput.setHeight(200.0);

        when(doorService.configureDoor(eq(1L), any(Size.class), any(Color.class),
                anyDouble(), anyDouble()))
            .thenReturn(door);

        // Act & Assert
        mockMvc.perform(post("/api/doors/1/configure")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(configInput)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    private DoorDto createTestDoorDto() {
        DoorDto dto = new DoorDto();
        dto.setName("Test Door");
        dto.setDescription("Test Description");
        dto.setPrice(100.0);
        dto.setSize(Size.STANDARD); 
        dto.setColor(Color.WHITE);
        dto.setMaterial("Wood");
        dto.setManufacturer("Test Manufacturer");
        dto.setWarrantyYears(2);
        return dto;
    }
}
