package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import uz.pdp.dto.BasketItemDTO;
import uz.pdp.dto.MouldingCreateDTO;
import uz.pdp.dto.MouldingDTO;
import uz.pdp.entity.Basket;
import uz.pdp.entity.Moulding;
import uz.pdp.entity.User;
import uz.pdp.enums.ItemType;
import uz.pdp.payload.EntityResponse;
import uz.pdp.repository.UserRepository;
import uz.pdp.service.BasketService;
import uz.pdp.service.MouldingService;
import uz.pdp.service.ImageStorageService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Controller for managing moulding operations.
 * Handles CRUD operations for mouldings, with role-based access control.
 * Because every moulding deserves its moment in the spotlight! 
 */
@RestController
@RequestMapping("/api/mouldings")
@Tag(name = "Moulding Controller", description = "API for managing mouldings")
@RequiredArgsConstructor
public class MouldingController {
    private final MouldingService mouldingService;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;
    private final BasketService basketService;

    private static final String MOULDING_IMAGES_PREFIX = "mouldings/";
    private final Logger logger = LoggerFactory.getLogger(MouldingController.class);

    /**
     * Get all mouldings with pagination.
     * Serving your mouldings one page at a time! 
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SELLER')")
    @Operation(summary = "Get all mouldings", description = "Retrieves a paginated list of all mouldings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved mouldings"),
        @ApiResponse(responseCode = "401", description = "Not authorized to view mouldings")
    })
    public EntityResponse<Page<MouldingDTO>> getAllMouldings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<MouldingDTO> mouldings = mouldingService.getAllMouldings(pageable);
            return EntityResponse.success("Mouldings retrieved successfully", mouldings);
        } catch (Exception e) {
            logger.error("Failed to retrieve mouldings: {}", e.getMessage());
            return EntityResponse.error("Failed to retrieve mouldings: " + e.getMessage());
        }
    }

    /**
     * Add a moulding to the user's basket.
     * Because every moulding deserves a chance to decorate someone's door! 🚪✨
     *
     * @param mouldingId The ID of the moulding to add
     * @param quantity The quantity to add
     * @return Response containing the updated basket
     */
    @PostMapping("/{mouldingId}/basket")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Add moulding to basket", description = "Adds specified quantity of moulding to user's basket")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully added to basket"),
        @ApiResponse(responseCode = "401", description = "Not authorized"),
        @ApiResponse(responseCode = "404", description = "Moulding not found")
    })
    public EntityResponse<Basket> addToBasket(
            @PathVariable Long mouldingId,
            @RequestParam(defaultValue = "1") int quantity) {
        try {
            BasketItemDTO itemDTO = new BasketItemDTO();
            itemDTO.setItemId(mouldingId);
            itemDTO.setType(ItemType.MOULDING);
            itemDTO.setQuantity(quantity);
            
            Basket updatedBasket = basketService.addItem(itemDTO);
            return EntityResponse.success("Moulding added to basket successfully", updatedBasket);
        } catch (Exception e) {
            logger.error("Failed to add moulding to basket: {}", e.getMessage());
            return EntityResponse.error("Failed to add moulding to basket: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SELLER')")
    @Operation(summary = "Get moulding by ID", description = "Retrieves a specific moulding by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved moulding"),
        @ApiResponse(responseCode = "404", description = "Moulding not found")
    })
    public EntityResponse<?> getMouldingById(@Parameter(description = "ID of the moulding") @PathVariable Long id) {
        Optional<Moulding> moulding = mouldingService.getMouldingById(id);
        if (moulding.isPresent()) {
            return EntityResponse.success("Moulding retrieved successfully", mouldingService.toDTO(moulding.get()));
        } else {
            return EntityResponse.error("Moulding not found", null);
        }
    }

    /**
     * Creates a new moulding entry.
     * Time to add another masterpiece to our collection! 
     *
     * @param dto The moulding details
     * @return The created moulding
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(summary = "Create a new moulding", description = "Creates a new moulding entry")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Moulding created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - requires ADMIN or SELLER role")
    })
    public EntityResponse<MouldingDTO> createMoulding(@RequestBody @Valid MouldingCreateDTO dto) {
        try {
            // Get current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

            Moulding createdMoulding = mouldingService.createMoulding(dto, currentUser);
            logger.info("Created new moulding: {} by user: {}", createdMoulding.getTitle(), username);
            return EntityResponse.success("Moulding created successfully", mouldingService.toDTO(createdMoulding));
        } catch (Exception e) {
            logger.error("Failed to create moulding: {}", e.getMessage());
            return EntityResponse.error("Failed to create moulding: " + e.getMessage());
        }
    }

    /**
     * Updates an existing moulding.
     * Time for a moulding makeover! 
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(summary = "Update a moulding", description = "Updates an existing moulding entry")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Moulding updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - requires ADMIN or SELLER role"),
        @ApiResponse(responseCode = "404", description = "Moulding not found")
    })
    public EntityResponse<MouldingDTO> updateMoulding(
            @PathVariable Long id,
            @RequestBody @Valid MouldingDTO dto) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

            Moulding updatedMoulding = mouldingService.updateMoulding(id, dto, currentUser);
            logger.info("Updated moulding: {} by user: {}", updatedMoulding.getTitle(), username);
            return EntityResponse.success("Moulding updated successfully", mouldingService.toDTO(updatedMoulding));
        } catch (Exception e) {
            logger.error("Failed to update moulding: {}", e.getMessage());
            return EntityResponse.error("Failed to update moulding: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(summary = "Delete a moulding", description = "Deletes a moulding by its ID")
    @ApiResponse(responseCode = "200", description = "Moulding deleted successfully")
    public EntityResponse<?> deleteMoulding(@Parameter(description = "ID of the moulding to delete") @PathVariable Long id) {
        mouldingService.deleteMoulding(id);
        return EntityResponse.success("Moulding deleted successfully");
    }

    /**
     * Uploads a moulding image to S3 storage.
     * Because every moulding deserves its glamour shot! 📸
     *
     * @param id The ID of the moulding to add the image to
     * @param file The image file to upload
     * @return Response containing the S3 URL of the uploaded image
     */
    @PostMapping(value = "/{id}/upload-attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")
    @Operation(summary = "Upload moulding image", description = "Uploads an image for a specific moulding to S3 storage")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file format or size"),
        @ApiResponse(responseCode = "403", description = "Not authorized to upload images for this moulding"),
        @ApiResponse(responseCode = "404", description = "Moulding not found")
    })
    public EntityResponse<MouldingDTO> uploadImage(
            @Parameter(description = "ID of the moulding") @PathVariable Long id,
            @Parameter(description = "Image file to upload") @RequestParam("file") MultipartFile file) {
        try {
            // Get current moulding
            Optional<Moulding> mouldingOpt = mouldingService.getMouldingById(id);
            if (mouldingOpt.isEmpty()) {
                return EntityResponse.error("Moulding not found");
            }
            Moulding moulding = mouldingOpt.get();

            // Get current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

            // Check if user is authorized
            if (!currentUser.getRole().name().equals("ADMIN") && 
                (moulding.getUser() == null || !moulding.getUser().getId().equals(currentUser.getId()))) {
                return EntityResponse.error("Not authorized to upload images for this moulding");
            }

            // Upload image
            String imageUrl = imageStorageService.storeMouldingImage(file);

            // Update moulding with new image
            MouldingDTO dto = mouldingService.toDTO(moulding);
            List<String> currentImages = new ArrayList<>(moulding.getImagesUrl());
            currentImages.add(imageUrl);
            dto.setImagesUrl(currentImages);

            // Save changes
            Moulding updatedMoulding = mouldingService.updateMoulding(id, dto, currentUser);
            return EntityResponse.success(
                "Strike a pose! 📸 Your moulding's new photo has been uploaded successfully! ✨", 
                mouldingService.toDTO(updatedMoulding)
            );
        } catch (IllegalArgumentException e) {
            logger.error("Invalid image upload request: {}", e.getMessage());
            return EntityResponse.error("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to upload image: {}", e.getMessage());
            return EntityResponse.error("Failed to upload image: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'SELLER')")  // Seller check is done in service layer
    @Operation(summary = "Update moulding images in S3 (ADMIN or owner SELLER)", 
              description = "Updates images for a moulding. Only ADMIN or the SELLER who owns the moulding can update.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Images updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or file type"),
            @ApiResponse(responseCode = "403", description = "Not authorized to update this moulding's images"),
            @ApiResponse(responseCode = "404", description = "Moulding not found or image not found"),
            @ApiResponse(responseCode = "500", description = "Failed to update images")
    })
    public EntityResponse<MouldingDTO> updateImages(
            @PathVariable Long id,
            @RequestParam(value = "deleteUrls", required = false) String deleteUrl,
            @RequestPart(value = "newImages", required = false) MultipartFile[] newImages) {
        try {
            // Get current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

            // Convert single URL to list
            List<String> deleteUrls = new ArrayList<>();
            if (deleteUrl != null && !deleteUrl.trim().isEmpty()) {
                deleteUrls.add(deleteUrl);
            }

            // Update images using service
            MouldingDTO updatedMoulding = mouldingService.updateMouldingImages(id, deleteUrls, newImages, currentUser);
            
            // Create appropriate success message
            String message;
            if (deleteUrl != null && !deleteUrl.trim().isEmpty()) {
                message = "Image successfully deleted! 🗑️";
            } else if (newImages != null && newImages.length > 0) {
                message = "New images successfully uploaded! 📸";
            } else {
                message = "No changes were made to the images";
            }
            
            return EntityResponse.success(message, updatedMoulding);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request for moulding image update: {}", e.getMessage());
            return EntityResponse.error(e.getMessage());
        } catch (IllegalStateException e) {
            logger.error("Authorization or processing error: {}", e.getMessage());
            return EntityResponse.error(e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to update moulding images: {}", e.getMessage(), e);
            return EntityResponse.error("An unexpected error occurred: " + e.getMessage());
        }
    }
}   