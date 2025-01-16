package uz.pdp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import uz.pdp.service.DoorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import uz.pdp.dto.DoorDto;
import uz.pdp.dto.UserDoorHistoryDto;
import uz.pdp.entity.Door;
import uz.pdp.entity.DoorHistory;
import uz.pdp.mutations.DoorConfigInput;
import uz.pdp.service.DoorHistoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import uz.pdp.payload.EntityResponse;
import uz.pdp.service.UserService;

import java.util.List;
import java.util.Arrays;

@RestController
@RequestMapping("/api/doors")
@Tag(name = "Door Management", description = "APIs for managing doors")
public class DoorController {

    private static final Logger logger = LoggerFactory.getLogger(DoorController.class);


    @Autowired
    private DoorService doorService;

    @Autowired
    private UserService userService;

    @Autowired
    private DoorHistoryService doorHistoryService;

    @GetMapping("/history")
    @Operation(summary = "Get user door's history", description = "Open to all users")
    public ResponseEntity<EntityResponse<UserDoorHistoryDto>> getUserDoorHistory() {
        Long userId = userService.getCurrentUser().getId();
        UserDoorHistoryDto history = doorHistoryService.getUserDoorHistoryGrouped(userId);
        if (history == null || (history.getHistory() != null && history.getHistory().isEmpty())) {
            return ResponseEntity.ok(EntityResponse.success("No history found for this user", null));
        }
        return ResponseEntity.ok(EntityResponse.success("History retrieved successfully", history));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get door details by ID", description = "Open to all users")
    public ResponseEntity<EntityResponse<?>> getDoor(@PathVariable Long id) {
        logger.info("Fetching door with id: {}", id);
        try {
            Door door = doorService.getDoor(id);
            doorHistoryService.saveDoorHistory(door);
            return ResponseEntity.ok(EntityResponse.success("Door retrieved successfully", door));
        } catch (EntityNotFoundException e) {
            logger.warn("Door not found with id: {}", id);
            return ResponseEntity.ok(EntityResponse.error("Door with ID " + id + " does not exist"));
        } catch (Exception e) {
            logger.error("Error while fetching door with id {}: {}", id, e.getMessage());
            return ResponseEntity.ok(EntityResponse.error("An error occurred while fetching the door"));
        }
    }

    @GetMapping
    @Operation(summary = "Get all doors", description = "Open to all users")
    public ResponseEntity<EntityResponse<Page<Door>>> getAllDoors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Door> doors = doorService.getAllDoors(page, size);
        return ResponseEntity.ok(EntityResponse.success("Doors retrieved successfully", doors));
    }

    @PostMapping
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    @Operation(summary = "Create a new door (SELLER and ADMIN only)")
    public ResponseEntity<EntityResponse<Door>> createDoor(@Validated @RequestBody DoorDto doorDto) {
        logger.info("Creating new door: {}", doorDto);
        Door door = doorService.createDoor(doorDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EntityResponse.success("Door created successfully", door));
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

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @doorSecurityService.isSeller(#id))")
    @Operation(summary = "Upload door images (ADMIN or owner SELLER)")
    public ResponseEntity<EntityResponse<Door>> uploadImages(
            @PathVariable Long id,
            @RequestPart("images") MultipartFile[] images) {
        logger.info("Uploading {} images for door ID: {}", images.length, id);
        Door door = doorService.addImages(id, Arrays.asList(images));
        return ResponseEntity.ok(EntityResponse.success("Images uploaded successfully", door));
    }
}