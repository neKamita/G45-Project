package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uz.pdp.dto.DoorDto;
import uz.pdp.dto.UserDoorHistoryDto;
import uz.pdp.dto.BasketItemDTO;
import uz.pdp.dto.BasketResponseDTO;
import uz.pdp.entity.Door;
import uz.pdp.entity.User;
import uz.pdp.enums.ItemType;
import uz.pdp.mutations.DoorConfigInput;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.BasketService;
import uz.pdp.service.DoorHistoryService;
import uz.pdp.service.DoorService;
import uz.pdp.service.UserService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * REST controller for managing smart door operations.
 * Provides endpoints for door management including creation, status updates,
 * and access control. Most operations require proper authentication and authorization.
 * 
 * Warning: This controller contains some serious door-related wizardry.
 * If you're brave enough to modify it, may the code gods be with you!
 *
 * @version 1.0
 * @since 2025-01-17
 */
@RestController
@RequestMapping("/api/doors")
@Tag(name = "Door Management", description = "APIs for managing smart doors and access control")
@Validated
public class DoorController {

    // For when shit hits the fan, we need receipts
    private static final Logger logger = LoggerFactory.getLogger(DoorController.class);

    // The holy trinity of services - touch them and you're in for a world of pain
    @Autowired
    private DoorService doorService;

    @Autowired
    private UserService userService;

    @Autowired
    private DoorHistoryService doorHistoryService;

    @Autowired
    private BasketService basketService;

    /**
     * Retrieves a user's door history because apparently, 
     * we need to track every damn time someone opens a door.
     * 
     * @return ResponseEntity with user's door history or a nice "you haven't touched any doors" message
     */
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

    /**
     * Retrieves a door's details by ID.
     * Open to all users.
     *
     * @param id Door ID to retrieve
     * @return ResponseEntity with door details
     *         - 200 OK with door details
     *         - 404 Not Found if door doesn't exist
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get door details by ID", description = "Open to all users")
    public ResponseEntity<EntityResponse<?>> getDoor(@PathVariable Long id) {
        logger.info("Fetching door with id: {}", id);
        try {
            Door door = doorService.getDoor(id);
            try {
                User currentUser = userService.getCurrentUser();
                if (currentUser != null) {
                    doorHistoryService.saveDoorHistory(door);
                }
            } catch (Exception e) {
                logger.debug("Not saving door history - user not authenticated");
            }
            return ResponseEntity.ok(EntityResponse.success("Door retrieved successfully", door));
        } catch (EntityNotFoundException e) {
            logger.error("Door not found with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(EntityResponse.error("Door not found: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error while fetching door with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EntityResponse.error("An error occurred while fetching the door"));
        }
    }

    /**
     * Retrieves similar doors to the specified door.
     * Open to all users.
     *
     * @param id Door ID to retrieve similar doors for
     * @param limit Number of similar doors to retrieve
     * @return ResponseEntity with list of similar doors
     *         - 200 OK with list of similar doors
     *         - 404 Not Found if door doesn't exist
     */
    @GetMapping("/{id}/similar")
    @Operation(summary = "Get similar doors", description = "Get doors similar to the specified door")
    public ResponseEntity<EntityResponse<List<Door>>> getSimilarDoors(
            @PathVariable Long id,
            @RequestParam(defaultValue = "5") int limit) {
        logger.info("Fetching similar doors for door id: {}, limit: {}", id, limit);
        try {
            List<Door> similarDoors = doorService.getSimilarDoors(id, limit);
            return ResponseEntity.ok(EntityResponse.success("Similar doors retrieved successfully", similarDoors));
        } catch (Exception e) {
            logger.error("Error while fetching similar doors for door id {}: {}", id, e.getMessage());
            return ResponseEntity.ok(EntityResponse.error("An error occurred while fetching similar doors"));
        }
    }

    /**
     * Retrieves all doors in the system.
     * Open to all users. Now with extra security! 🚪🔒
     * No sensitive seller data exposed here, folks!
     *
     * @param page Page number to retrieve
     * @param size Number of doors to retrieve per page
     * @return ResponseEntity with list of door DTOs
     *         - 200 OK with list of doors
     */
    @GetMapping
    @Operation(summary = "Get all doors", description = "Open to all users")
    public ResponseEntity<EntityResponse<Page<DoorDto>>> getAllDoors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            logger.info("Fetching doors page {} with size {}", page, size);
            Page<Door> doors = doorService.getAllDoors(page, size);
            Page<DoorDto> doorDtos = doors.map(DoorDto::fromEntity);
            return ResponseEntity.ok(EntityResponse.success("Doors retrieved successfully", doorDtos));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid pagination parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(EntityResponse.error("Invalid pagination parameters: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error fetching doors: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EntityResponse.error("Failed to fetch doors: " + e.getMessage()));
        }
    }

    /**
     * Creates a new door in the system.
     * Only for the chosen ones (ADMIN and SELLER roles).
     * 
     * Pro tip: If this fails, try turning it off and on again
     * 
     * @param doorDto The door's specs (please fill all fields, we're not mind readers)
     * @return A shiny new door object if everything goes well
     * @throws RuntimeException when the door gods are angry
     */
    @PostMapping
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    @Operation(summary = "Create a new door")
    public EntityResponse<Door> createDoor(@Valid @RequestBody DoorDto doorDto) {
        try {
            logger.info("Creating new door: {}", doorDto);
            Door door = doorService.createDoor(doorDto);
            return EntityResponse.success("Door created successfully", door);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid door data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating door: {}", e.getMessage());
            throw new RuntimeException("Failed to create door: " + e.getMessage());
        }
    }

    /**
     * Updates an existing door's settings.
     * Only administrators and the door's owner can update door settings.
     *
     * @param id Door ID to update
     * @param doorDto Updated door details
     * @return ResponseEntity with updated door
     *         - 200 OK if door updated successfully
     *         - 400 Bad Request if validation fails
     *         - 404 Not Found if door doesn't exist
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    @Operation(summary = "Update an existing door")
    public ResponseEntity<EntityResponse<Door>> updateDoor(
            @PathVariable Long id,
            @Valid @RequestBody DoorDto doorDto) {
        try {
            logger.info("Updating door {}: {}", id, doorDto);
            Door door = doorService.updateDoor(id, doorDto);
            return ResponseEntity.ok(EntityResponse.success("Door updated successfully", door));
        } catch (EntityNotFoundException e) {
            logger.error("Door not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(EntityResponse.error("Door not found: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid door data: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(EntityResponse.error("Invalid door data: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating door: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EntityResponse.error("Failed to update door: " + e.getMessage()));
        }
    }

    /**
     * The nuclear option - deletes a door from existence.
     * Only admins and the door's creator can perform this sacred ritual.
     * 
     * @param id The ID of the door to be yeeted from the database
     * @return Success message or a list of reasons why you can't delete someone else's door
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @doorSecurityService.isSeller(#id))")
    @Operation(summary = "Delete a door")
    public ResponseEntity<EntityResponse<Void>> deleteDoor(@PathVariable Long id) {
        doorService.deleteDoor(id);
        return ResponseEntity.ok(EntityResponse.success("Door deleted successfully"));
    }

    /**
     * Configures a door's size and color.
     * Only administrators and the door's owner can configure doors.
     *
     * @param id Door ID to configure
     * @param configInput Configuration details including size and color
     * @return ResponseEntity with configured door
     *         - 200 OK if door configured successfully
     *         - 400 Bad Request if validation fails
     *         - 404 Not Found if door doesn't exist
     */
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

    /**
     * Uploads images for a door.
     * Only administrators and the door's owner can upload images.
     *
     * @param id Door ID to upload images for
     * @param images Images to upload
     * @return ResponseEntity with updated door
     *         - 200 OK if images uploaded successfully
     *         - 400 Bad Request if validation fails
     *         - 404 Not Found if door doesn't exist
     */
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @doorSecurityService.isSeller(#id))")
    @Operation(summary = "Upload door images (ADMIN or owner SELLER)")
    public ResponseEntity<EntityResponse<Door>> uploadImages(
            @PathVariable Long id,
            @RequestPart("images") MultipartFile[] images) {
        try {
            logger.info("Uploading {} images for door ID: {}", images.length, id);
            Door updatedDoor = doorService.uploadImages(id, Arrays.asList(images));
            return ResponseEntity.ok(EntityResponse.success("Images uploaded successfully", updatedDoor));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid image upload request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(EntityResponse.error(e.getMessage()));
        } catch (EntityNotFoundException e) {
            logger.error("Door not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(EntityResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error uploading images: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EntityResponse.error("Failed to upload images: " + e.getMessage()));
        }
    }

    /**
     * Deletes images for a door.
     * Only administrators and the door's owner can delete images.
     *
     * @param id Door ID to delete images for
     * @param imageUrls URLs of images to delete
     * @return ResponseEntity with updated door
     *         - 200 OK if images deleted successfully
     *         - 400 Bad Request if validation fails
     *         - 404 Not Found if door doesn't exist
     */
    @DeleteMapping("/{id}/images")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @doorSecurityService.isSeller(#id))")
    @Operation(summary = "Delete door images (ADMIN or owner SELLER)")
    public ResponseEntity<EntityResponse<Door>> deleteImages(
            @PathVariable Long id,
            @RequestBody List<String> imageUrls) {
        logger.info("Deleting {} images from door ID: {}", imageUrls.size(), id);
        try {
            Door door = doorService.deleteImages(id, imageUrls);
            return ResponseEntity.ok(EntityResponse.success("Images deleted successfully", door));
        } catch (Exception e) {
            logger.error("Error deleting images: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(EntityResponse.error("Failed to delete images: " + e.getMessage()));
        }
    }

    /**
     * Updates images for a door.
     * Only administrators and the door's owner can update images.
     *
     * @param id Door ID to update images for
     * @param deleteUrls URLs of images to delete
     * @param newImages New images to upload
     * @return ResponseEntity with updated door
     *         - 200 OK if images updated successfully
     *         - 400 Bad Request if validation fails
     *         - 404 Not Found if door doesn't exist
     */
    @PutMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @doorSecurityService.isSeller(#id))")
    @Operation(summary = "Update door images (ADMIN or owner SELLER)")
    public ResponseEntity<EntityResponse<Door>> updateImages(
            @PathVariable Long id,
            @RequestParam(required = false) List<String> deleteUrls,
            @RequestPart("newImages") MultipartFile[] newImages) {
        logger.info("Updating images for door ID: {}, deleting {} images, adding {} new images",
                id, deleteUrls != null ? deleteUrls.size() : 0, newImages.length);
        try {
            Door door = doorService.updateImages(id,
                    deleteUrls != null ? deleteUrls : Collections.emptyList(),
                    Arrays.asList(newImages));
            return ResponseEntity.ok(EntityResponse.success("Images updated successfully", door));
        } catch (Exception e) {
            logger.error("Error updating images: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(EntityResponse.error("Failed to update images: " + e.getMessage()));
        }
    }

    /**
     * Adds a door to the user's shopping basket.
     * Because who doesn't need another door in their life? 🚪
     * 
     * @param id The ID of the door to add
     * @param quantity Number of doors to add (default: 1)
     * @return Response containing the updated basket
     * 
     *         🚪 → 🛒 Door successfully added to basket!
     *         Hope you have enough walls for all these doors! 😄
     */
    @PostMapping("/{id}/basket")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SELLER')")
    @Operation(summary = "Add door to basket", description = "Adds specified door to the user's shopping basket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Door added to basket successfully"),
            @ApiResponse(responseCode = "404", description = "Door not found"),
            @ApiResponse(responseCode = "400", description = "Invalid quantity"),
            @ApiResponse(responseCode = "403", description = "Access denied - User not authenticated")
    })
    public ResponseEntity<BasketResponseDTO> addToBasket(
            @Parameter(description = "ID of the door to add to basket") @PathVariable Long id,
            @Parameter(description = "Quantity to add") @RequestParam(defaultValue = "1") int quantity) {
        
        // First check if the door exists
        doorService.getDoorById(id);
        
        BasketItemDTO itemDTO = new BasketItemDTO(id, ItemType.DOOR, quantity);
        return ResponseEntity.ok(BasketResponseDTO.fromBasket(basketService.addItem(itemDTO)));
    }
}