package uz.pdp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import uz.pdp.dto.DoorDto;
import uz.pdp.entity.Door;
import uz.pdp.mutations.DoorConfigInput;
import uz.pdp.service.DoorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import uz.pdp.payload.EntityResponse;

import java.util.List;
import java.util.Arrays;

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
        logger.info("Fetching door with id: {}", id);
        Door door = doorService.getDoor(id);
        return ResponseEntity.ok(EntityResponse.success("Door retrieved successfully", door));
    }

    @GetMapping
    @Operation(summary = "Get all doors", description = "Open to all users")
    public ResponseEntity<EntityResponse<List<Door>>> getAllDoors() {
        logger.info("Fetching all doors");
        List<Door> doors = doorService.getAllDoors();
        logger.debug("Retrieved {} doors", doors.size());
        return ResponseEntity.ok(EntityResponse.success("Doors retrieved successfully", doors));
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Create a new door with images (SELLER only)")
    public ResponseEntity<EntityResponse<Door>> createDoorWithImages(
            @Valid @RequestPart("door") DoorDto doorDto,
            @RequestPart("images") MultipartFile[] images) {
        logger.info("Creating new door with images: {}", doorDto);
        Door door = doorService.createDoor(doorDto);
        door = doorService.addImages(door.getId(), Arrays.asList(images));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EntityResponse.success("Door created with images successfully", door));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @doorSecurityService.isSeller(#id))")
    @Operation(summary = "Update door details (ADMIN or owner SELLER)")
    public ResponseEntity<EntityResponse<Door>> updateDoor(
            @PathVariable Long id,
            @Valid @RequestBody DoorDto doorDto) {
        logger.info("Updating door with id: {}", id);
        Door updatedDoor = doorService.updateDoor(id, doorDto);
        logger.info("Successfully updated door with id: {}", id);
        return ResponseEntity.ok(EntityResponse.success("Door updated successfully", updatedDoor));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @doorSecurityService.isSeller(#id))")
    @Operation(summary = "Delete a door (ADMIN or owner SELLER)")
    public ResponseEntity<EntityResponse<Void>> deleteDoor(@PathVariable Long id) {
        logger.info("Deleting door with id: {}", id);
        doorService.deleteDoor(id);
        logger.info("Successfully deleted door with id: {}", id);
        return ResponseEntity.ok(EntityResponse.success("Door deleted successfully"));
    }

    @PostMapping("/{id}/configure")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @doorSecurityService.isSeller(#id))")
    @Operation(summary = "Configure door size and color (ADMIN or owner SELLER)")
    public ResponseEntity<EntityResponse<Door>> configureDoor(
            @PathVariable Long id,
            @Valid @RequestBody DoorConfigInput configInput
    ) {
        logger.info("Configuring door with ID: {}, configuration: {}", id, configInput);
        try {
            Door configuredDoor = doorService.configureDoor(
                    id,
                    configInput.getSize(),
                    configInput.getColor(),
                    configInput.getWidth(),
                    configInput.getHeight()
            );
            return ResponseEntity.ok(EntityResponse.success("Door configured successfully", configuredDoor));
        } catch (Exception e) {
            logger.error("Error configuring door: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(EntityResponse.error("Failed to configure door: " + e.getMessage()));
        }
    }


}