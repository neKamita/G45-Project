package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import uz.pdp.dto.FurnitureDoorCreateDTO;
import uz.pdp.dto.FurnitureDoorResponseDTO;
import uz.pdp.dto.BasketItemDTO;
import uz.pdp.dto.BasketResponseDTO;
import uz.pdp.entity.FurnitureDoor;
import uz.pdp.entity.Basket;
import uz.pdp.enums.ItemType;
import uz.pdp.exception.GlobalExceptionHandler.FurnitureDoorNotFoundException;
import uz.pdp.mapper.FurnitureDoorMapper;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.BasketService;
import uz.pdp.service.FurnitureDoorService;
import uz.pdp.service.ImageStorageService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for managing door accessories.
 * 
 * Fun fact: These accessories make your doors feel special! 
 * Like jewelry, but for doors! 
 * 
 * Warning: May cause your doors to become fashion-conscious.
 * Side effects include: increased door confidence and neighbor envy.
 */
@RestController
@RequestMapping("/api/door-accessories")
@RequiredArgsConstructor
@Tag(name = "Door Accessory Controller", description = "API endpoints for managing door accessories (handles, hinges, locks, etc.)")
@Validated
@Slf4j
public class DoorAccessoryController {

    private final FurnitureDoorService furnitureDoorService;
    private final FurnitureDoorMapper furnitureDoorMapper;
    private final ImageStorageService imageStorageService;
    private final BasketService basketService;


    Logger logger = LoggerFactory.getLogger(DoorAccessoryController.class);

    /**
     * Creates a new furniture door entry.
     * 
     * @param createDTO The door details for creation
     * @return Response containing the newly created door
     * 
     *         Door Factory API - Building dreams, one door at a time!
     *         Pro tip: Measure twice, POST once!
     */
    @Operation(summary = "Create a new door accessory", description = "Creates a new door accessory or furniture component")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Door accessory created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/door-accessories")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<EntityResponse<FurnitureDoorResponseDTO>> create(
            @Valid @RequestBody FurnitureDoorCreateDTO createDTO) {

        FurnitureDoor door = furnitureDoorMapper.toEntity(createDTO);
        FurnitureDoor created = furnitureDoorService.create(door);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EntityResponse.success(
                        "Door accessory created successfully! Ready to make doors fancy! ",
                        furnitureDoorMapper.toDto(created)));
    }

    /**
     * Uploads an image for a door accessory.
     * 
     * @param id    The ID of the door accessory
     * @param image The image file to upload
     * @return Response containing the updated accessory
     */
    @Operation(summary = "Upload door accessory image", description = "Upload an image for a door accessory")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid image format"),
            @ApiResponse(responseCode = "404", description = "Door accessory not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/door-accessories/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EntityResponse<FurnitureDoorResponseDTO>> uploadImage(
            @PathVariable Long id,
            @RequestPart("image") MultipartFile image) throws IOException {

        FurnitureDoor door = furnitureDoorService.getById(id)
                .orElseThrow(() -> new FurnitureDoorNotFoundException(id));

        // Validate image content type
        String contentType = validateAndGetContentType(image);

        // Upload new image
        String imageUrl;
        try {
            imageUrl = imageStorageService.storeAccessoryImage(image);
            logger.info("Successfully uploaded new image: {}", imageUrl);
        } catch (IOException e) {
            logger.error("Failed to upload new image: {}", e.getMessage());
            throw new IllegalStateException("Failed to upload new image: " + e.getMessage());
        }

        // Update door with new image - keeping existing ones
        FurnitureDoor updatedDoor = new FurnitureDoor();
        updatedDoor.setId(door.getId());
        updatedDoor.setName(door.getName());
        updatedDoor.setMaterial(door.getMaterial());
        updatedDoor.setDescription(door.getDescription());
        updatedDoor.setPrice(door.getPrice());
        updatedDoor.setDimensions(door.getDimensions());
        updatedDoor.setStockQuantity(door.getStockQuantity());
        updatedDoor.setFurnitureType(door.getFurnitureType());

        // Create new list with existing images plus new one
        List<String> imageUrls = new ArrayList<>(door.getImages() != null ? door.getImages() : new ArrayList<>());
        imageUrls.add(imageUrl); // Add the new image to the collection
        updatedDoor.setImages(imageUrls);

        FurnitureDoor updated = furnitureDoorService.update(id, updatedDoor);
        return ResponseEntity.ok(EntityResponse.success(
                "Image added successfully! Your door's photo album is growing! ",
                furnitureDoorMapper.toDto(updated)));
    }

    /**
     * Validates and determines the content type of an uploaded image.
     * Because every image deserves a proper ID check! 
     *
     * @param image The image file to validate
     * @return The determined content type
     * @throws IllegalArgumentException if the image type is invalid
     */
    private String validateAndGetContentType(MultipartFile image) {
        String contentType = image.getContentType();
        if (contentType == null || contentType.equals("application/octet-stream")) {
            String fileName = image.getOriginalFilename();
            if (fileName != null) {
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                switch (extension) {
                    case "jpg":
                    case "jpeg":
                        contentType = "image/jpeg";
                        break;
                    case "png":
                        contentType = "image/png";
                        break;
                    case "gif":
                        contentType = "image/gif";
                        break;
                    default:
                        logger.error("Invalid file extension: {}", extension);
                        throw new IllegalArgumentException(
                                "Sorry, this file type isn't invited to the door party! Only JPG, PNG, and GIF get VIP access! ");
                }
            }
        }

        if (!contentType.startsWith("image/")) {
            logger.error("Invalid content type: {}", contentType);
            throw new IllegalArgumentException(
                    "Hey, that's not an image! Our doors only wear proper image accessories! ");
        }

        return contentType;
    }

    /**
     * Retrieves all furniture doors from the database with pagination.
     * 
     * @param page Page number to retrieve (0-based)
     * @param size Number of items per page
     * @return Response containing paginated list of available doors
     * 
     *         Open the gates, let them all out!
     *         Warning: May return more doors than your house has walls
     */
    @Operation(summary = "Get all furniture doors", description = "Retrieves a paginated list of all available furniture doors")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/door-accessories")
    public ResponseEntity<EntityResponse<Page<FurnitureDoorResponseDTO>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<FurnitureDoorResponseDTO> doors = furnitureDoorService.getAll(page, size)
                .map(furnitureDoorMapper::toDto);
        return ResponseEntity.ok(EntityResponse.success(
                String.format("Found %d door accessories on page %d! ", doors.getNumberOfElements(), page + 1), 
                doors));
    }

    /**
     * Finds a specific door by its ID.
     * 
     * @param id The ID of the door to retrieve
     * @return Response containing the found door
     * 
     *         Door Detective at work!
     *         Sometimes doors play hide and seek, but we always find them!
     */
    @Operation(summary = "Get a furniture door by ID", description = "Retrieves a specific furniture door by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the door"),
            @ApiResponse(responseCode = "404", description = "Door not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/door-accessories/{id}")
    public ResponseEntity<EntityResponse<FurnitureDoorResponseDTO>> getById(
            @Parameter(description = "ID of the door to retrieve") @PathVariable Long id) {
        return furnitureDoorService.getById(id)
                .map(door -> ResponseEntity.ok(EntityResponse.success(
                        "Door found and ready for inspection!",
                        furnitureDoorMapper.toDto(door))))
                .orElseThrow(() -> new FurnitureDoorNotFoundException(id));
    }

    /**
     * Updates an existing door's information.
     * 
     * @param id        The ID of the door to update
     * @param updateDTO The new door details
     * @return Response containing the updated door
     * 
     *         Extreme Makeover: Door Edition!
     *         Making old doors feel young again
     */
    @Operation(summary = "Update a furniture door", description = "Updates an existing furniture door with new details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Door updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Door not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/door-accessories/{id}")
    public ResponseEntity<EntityResponse<FurnitureDoorResponseDTO>> update(
            @Parameter(description = "ID of the door to update") @PathVariable Long id,
            @Valid @RequestBody FurnitureDoorCreateDTO updateDTO) {
        FurnitureDoor door = furnitureDoorMapper.toEntity(updateDTO);
        return furnitureDoorService.getById(id)
                .map(existingDoor -> {
                    FurnitureDoor updated = furnitureDoorService.update(id, door);
                    return ResponseEntity.ok(EntityResponse.success(
                            "Door makeover complete!",
                            furnitureDoorMapper.toDto(updated)));
                })
                .orElseThrow(() -> new FurnitureDoorNotFoundException(id));
    }

    /**
     * Deletes a door from the system.
     * 
     * @param id The ID of the door to delete
     * @return Response indicating successful deletion
     * 
     *         Goodbye door, you served us well!
     *         Don't worry, it's going to door heaven
     *         P.S. No doors were harmed in the making of this API
     */
    @Operation(summary = "Delete a furniture door", description = "Deletes a furniture door by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Door deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Door not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/door-accessories/{id}")
    public ResponseEntity<EntityResponse<Void>> delete(
            @Parameter(description = "ID of the door to delete") @PathVariable Long id) {
        furnitureDoorService.delete(id);
        return ResponseEntity.ok(EntityResponse.success("Door has been retired successfully!"));
    }

    /**
     * Adds an accessory to the user's shopping basket.
     * 
     * @param id The ID of the accessory to add
     * @param quantity Number of accessories to add
     * @return Response containing the updated basket
     * 
     *         Adding some bling to your basket!
     *         Your door is going to look fabulous! 
     */
    @PostMapping("/{id}/basket")
    @Operation(summary = "Add accessory to basket", description = "Adds specified accessory to the user's shopping basket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accessory added to basket successfully"),
            @ApiResponse(responseCode = "404", description = "Accessory not found"),
            @ApiResponse(responseCode = "400", description = "Invalid quantity")
    })
    public ResponseEntity<BasketResponseDTO> addToBasket(
            @Parameter(description = "ID of the accessory to add to basket") @PathVariable Long id,
            @Parameter(description = "Quantity to add") @RequestParam(defaultValue = "1") int quantity) {
        
        BasketItemDTO itemDTO = new BasketItemDTO(id, ItemType.DOOR_ACCESSORY, quantity);
        return ResponseEntity.ok(BasketResponseDTO.fromBasket(basketService.addItem(itemDTO)));
    }
}
