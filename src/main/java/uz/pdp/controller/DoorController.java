package uz.pdp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import uz.pdp.entity.Door;
import uz.pdp.mutations.DoorConfigInput;
import uz.pdp.service.DoorService;
import org.springframework.http.HttpStatus;
import uz.pdp.payload.EntityResponse;

import java.util.List;

@RestController
@RequestMapping("/api/doors")
@Tag(name = "Door Management", description = "APIs for managing doors")
public class DoorController {

    private static final Logger logger = LoggerFactory.getLogger(DoorController.class);

    @Autowired
    private DoorService doorService;

    @GetMapping("/{id}")
    @Operation(summary = "Get door details by ID", description = "Open to all users")
    public ResponseEntity<EntityResponse<Door>> getDoor(@PathVariable Long id) {
        Door door = doorService.getDoor(id);
        return ResponseEntity.ok(EntityResponse.success(door));
    }

    @GetMapping
    @Operation(summary = "Get all doors", description = "Open to all users")
    public ResponseEntity<EntityResponse<List<Door>>> getAllDoors() {
        List<Door> doors = doorService.getAllDoors();
        return ResponseEntity.ok(EntityResponse.success(doors));
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Create a new door (SELLER only)")
    public ResponseEntity<EntityResponse<Door>> createDoor(@Valid @RequestBody Door door) {
        logger.info("Creating new door: {}", door);
        Door createdDoor = doorService.createDoor(door);
        logger.info("Door created with ID: {}", createdDoor.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(EntityResponse.created("Door created successfully", createdDoor));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @doorSecurityService.isSeller(#id))")
    @Operation(summary = "Update door details (ADMIN or owner SELLER)")
    public ResponseEntity<EntityResponse<Door>> updateDoor(@PathVariable Long id, @RequestBody Door door) {
        Door updatedDoor1 = doorService.updateDoor(id, door);
        return ResponseEntity.ok(EntityResponse.success("Door updated successfully", updatedDoor1));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a door")
    public ResponseEntity<EntityResponse<Void>> deleteDoor(@PathVariable Long id) {
        doorService.deleteDoor(id);
        return ResponseEntity.ok(EntityResponse.deleted());
    }

    @PostMapping("/{id}/configure")
    @Operation(summary = "Configure door size and color")
    public ResponseEntity<EntityResponse<Door>> configureDoor(
            @PathVariable Long id,
            @RequestBody DoorConfigInput configInput) {
        logger.info("Configuring door with ID: {}, configuration: {}", id, configInput);
        Door configuredDoor = doorService.configureDoor(
            id,
            configInput.getSize(),
            configInput.getColor(),
            configInput.getWidth(),
            configInput.getHeight()
        );
        logger.info("Configured door: {}", configuredDoor);
        return ResponseEntity.ok(EntityResponse.success("Door configured successfully", configuredDoor));
    }

 


}