package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import uz.pdp.dto.FurnitureDoorCreateDTO;
import uz.pdp.dto.FurnitureDoorResponseDTO;
import uz.pdp.entity.FurnitureDoor;
import uz.pdp.exception.GlobalExceptionHandler.FurnitureDoorNotFoundException;
import uz.pdp.mapper.FurnitureDoorMapper;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.FurnitureDoorService;
import uz.pdp.service.ImageStorageService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for managing door accessories and furniture.
 * Welcome to the Door Accessories API - Where we make doors fancy!
 */
@RestController
@RequiredArgsConstructor
@Validated
@Tag(name = "Door Accessories Controller", description = "API endpoints for managing door accessories and furniture")
public class FurnitureDoorController {

    private final FurnitureDoorService furnitureDoorService;
    private final FurnitureDoorMapper furnitureDoorMapper;
    private final ImageStorageService imageStorageService;

    /**
     * Creates a new furniture door entry.
     * 
     * @param createDTO The door details for creation
     * @return Response containing the newly created door
     * 
     *         Door Factory API - Building dreams, one door at a time!
     *         Pro tip: Measure twice, POST once!
     */
    @Operation(summary = "Create a new furniture door", description = "Creates a new furniture door")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Door created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/api/door-accessories")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<EntityResponse<FurnitureDoorResponseDTO>> create(
            @Valid @RequestBody FurnitureDoorCreateDTO createDTO) {

        FurnitureDoor door = furnitureDoorMapper.toEntity(createDTO);
        FurnitureDoor created = furnitureDoorService.create(door);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(EntityResponse.success(
                        "Door created successfully! Ready for its glamour shot! ",
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
    @PostMapping(value = "/api/door-accessories/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EntityResponse<FurnitureDoorResponseDTO>> uploadImage(
            @PathVariable Long id,
            @RequestPart("image") MultipartFile image) throws IOException {

        FurnitureDoor door = furnitureDoorService.getById(id)
                .orElseThrow(() -> new FurnitureDoorNotFoundException(id));

        // Handle image upload
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
                        throw new IllegalArgumentException(
                                "Invalid file type. Only JPG, PNG, and GIF are allowed! ");
                }
            }
        }

        if (!contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Invalid file type. Only images are allowed! ");
        }

        // Delete old image if exists
        if (door.getImageUrls() != null && !door.getImageUrls().isEmpty()) {
            try {
                for (String oldImageUrl : door.getImageUrls()) {
                    imageStorageService.deleteImage(oldImageUrl);
                }
            } catch (Exception e) {
                // logger.warn("Failed to delete old images: {}", e.getMessage());
            }
        }

        String imageUrl = imageStorageService.storeImage(image);

        // Time to play dress-up with our door!
        // First, let's keep all the door's existing fancy features
        FurnitureDoor updatedDoor = new FurnitureDoor();
        updatedDoor.setId(door.getId()); // ID card - don't lose it!
        updatedDoor.setName(door.getName()); // A door by any other name...
        updatedDoor.setMaterial(door.getMaterial()); // The stuff it's made of
        updatedDoor.setDescription(door.getDescription()); // Its life story
        updatedDoor.setPrice(door.getPrice()); // Ka-ching!
        updatedDoor.setDimensions(door.getDimensions()); // Size matters!
        updatedDoor.setStockQuantity(door.getStockQuantity()); // How many clones we have
        updatedDoor.setFurnitureType(door.getFurnitureType()); // What kind of door-some creature it is

        // Now for the glamour shot!
        List<String> imageUrls = new ArrayList<>(door.getImageUrls()); // Keep existing images
        imageUrls.add(imageUrl); // Add the new one
        updatedDoor.setImageUrls(imageUrls); // Set all images

        List<String> fileNames = new ArrayList<>(door.getOriginalFileNames()); // Keep existing filenames
        fileNames.add(image.getOriginalFilename()); // Add the new one
        updatedDoor.setOriginalFileNames(fileNames); // Set all filenames

        // Save our door's new look to the fashion catalog
        FurnitureDoor updated = furnitureDoorService.update(id, updatedDoor);
        return ResponseEntity.ok(EntityResponse.success(
                "Image uploaded successfully! Looking fabulous!  Time for your door's Instagram debut! ",
                furnitureDoorMapper.toDto(updated)));
    }

    /**
     * Retrieves all furniture doors from the database.
     * 
     * @return Response containing list of all available doors
     * 
     *         Open the gates, let them all out!
     *         Warning: May return more doors than your house has walls
     */
    @Operation(summary = "Get all furniture doors", description = "Retrieves a list of all available furniture doors")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/api/door-accessories")
    public ResponseEntity<EntityResponse<List<FurnitureDoorResponseDTO>>> getAll() {
        List<FurnitureDoorResponseDTO> doors = furnitureDoorService.getAll()
                .stream()
                .map(furnitureDoorMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(EntityResponse.success("Found " + doors.size() + " doors!", doors));
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
    @GetMapping("/api/door-accessories/{id}")
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
    @PutMapping("/api/door-accessories/{id}")
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
    @DeleteMapping("/api/door-accessories/{id}")
    public ResponseEntity<EntityResponse<Void>> delete(
            @Parameter(description = "ID of the door to delete") @PathVariable Long id) {
        furnitureDoorService.delete(id);
        return ResponseEntity.ok(EntityResponse.success("Door has been retired successfully!"));
    }
}
