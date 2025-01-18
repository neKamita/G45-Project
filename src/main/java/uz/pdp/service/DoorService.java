package uz.pdp.service;

import java.util.List;
import java.util.ArrayList;
import uz.pdp.exception.ResourceNotFoundException;
import uz.pdp.exception.BadRequestException;
import uz.pdp.exception.ConflictException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import uz.pdp.dto.DoorDto;
import uz.pdp.entity.Door;
import uz.pdp.entity.User;
import uz.pdp.enums.Color;
import uz.pdp.enums.Size;
import uz.pdp.enums.DoorStatus;
import uz.pdp.repository.DoorHistoryRepository;
import uz.pdp.repository.DoorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.SneakyThrows;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

/**
 * Service class for managing door operations.
 * Handles door creation, configuration, access control, and image management.
 * Implements security checks and maintains audit logs for door operations.
 *
 * @version 1.0
 * @since 2025-01-17
 */
@Service
public class DoorService {
    private static final Logger logger = LoggerFactory.getLogger(DoorService.class);

    @Autowired
    private DoorRepository doorRepository;
    
    @Autowired
    private UserService userService;

    @Autowired
    private ImageStorageService imageStorageService;

    @Autowired
    private DoorHistoryRepository doorHistoryRepository;

    /**
     * Retrieves a door by its ID.
     * Internal helper method for door operations.
     *
     * @param id Door ID to retrieve
     * @return Door entity
     * @throws ResourceNotFoundException if door not found
     */
    // Open to all users - no @PreAuthorize needed
    @Transactional(readOnly = true)
    public Door getDoor(Long id) {
        return doorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Door not found with id: " + id));
    }

    /**
     * Retrieves all doors accessible to the current user.
     * Admins can see all doors, while regular users only see doors they have access to.
     *
     * @return Page of accessible doors
     */
    // Open to all users - no @PreAuthorize needed
    @Transactional(readOnly = true)
    public Page<Door> getAllDoors(int page, int size) {
        return doorRepository.findAll(PageRequest.of(page, size));
    }

    /**
     * Creates a new door.
     * Validates door details and sets up initial configuration.
     *
     * @param doorDto Door details including location and access settings
     * @return Door entity
     * @throws BadRequestException if validation fails
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    @CachePut(value = "doors", key = "#result.id")
    public Door createDoor(DoorDto doorDto) {
        if (doorDto == null) {
            throw new BadRequestException("Door data cannot be null");
        }

        User currentUser = userService.getCurrentUser();
        Door door = new Door();
        mapDtoToEntity(doorDto, door);
        door.setSeller(currentUser);
        door.calculateFinalPrice();
        Door savedDoor = doorRepository.saveAndFlush(door);
        logger.info("Door created with ID: {}", savedDoor.getId());
        return savedDoor;
    }

    /**
     * Maps DTO fields to Door entity.
     *
     * @param dto DTO containing door details
     * @param door Door entity to update
     */
    private void mapDtoToEntity(DoorDto dto, Door door) {
        door.setName(dto.getName());
        door.setDescription(dto.getDescription());
        door.setPrice(dto.getPrice());
        door.setSize(dto.getSize());
        door.setColor(dto.getColor());
        door.setMaterial(dto.getMaterial());
        door.setManufacturer(dto.getManufacturer());
        door.setWarrantyYears(dto.getWarrantyYears());
        door.setCustomWidth(dto.getCustomWidth());
        door.setCustomHeight(dto.getCustomHeight());
        door.setIsCustomColor(dto.getIsCustomColor());
    }

    /**
     * Updates an existing door's settings.
     * Validates update permissions and door configuration.
     *
     * @param id Door ID to update
     * @param doorDto Updated door details
     * @return Door entity
     * @throws BadRequestException if door not found or validation fails
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @doorSecurityService.isSeller(#id))")
    @CachePut(value = "doors", key = "#id")
    public Door updateDoor(Long id, DoorDto doorDto) {
        if (doorDto == null) {
            throw new BadRequestException("Door data cannot be null");
        }

        logger.info("Updating door with ID: {}, data: {}", id, doorDto);
        Door door = getDoor(id);
        mapDtoToEntity(doorDto, door);
        door.calculateFinalPrice();
        Door savedDoor = doorRepository.save(door);
        logger.info("Door with ID {} updated.", id);
        return savedDoor;
    }

    /**
     * Deletes a door from the system.
     * Performs cleanup of associated resources including images and door history.
     *
     * @param id Door ID to delete
     * @throws ConflictException if door is not available
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @doorSecurityService.isSeller(#id))")
    @CacheEvict(value = {"doors", "allDoors"}, allEntries = true)
    public void deleteDoor(Long id) {
        logger.info("Deleting door with ID: {}", id);
        Door door = getDoor(id);
        
        if (!door.isActive()) {
            throw new ConflictException("Cannot delete an inactive door");
        }
        
        // Delete associated door history first
        doorHistoryRepository.deleteByDoorId(id);
        
        // Now delete the door
        doorRepository.delete(door);
        logger.info("Door with ID {} and its history deleted successfully", id);
    }

    /**
     * Configures a door's settings.
     * Validates configuration and updates door entity.
     *
     * @param id Door ID to configure
     * @param size Door size
     * @param color Door color
     * @param width Custom width
     * @param height Custom height
     * @return Door entity
     * @throws BadRequestException if validation fails
     */
    @Transactional(rollbackFor = Exception.class)
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @doorSecurityService.isSeller(#id))")
    @CachePut(value = "doors", key = "#id")
    @SneakyThrows
    public Door configureDoor(Long id, Size size, Color color, Double width, Double height) {
        logger.info("Configuring door with ID: {}, size: {}, color: {}, width: {}, height: {}", 
            id, size, color, width, height);
        Door door = getDoor(id);
        
        if (size != null) {
            door.setSize(size);
            if (size == Size.CUSTOM) {
                if (width == null || height == null) {
                    throw new BadRequestException("Custom size requires both width and height to be specified");
                }
                door.setCustomWidth(width);
                door.setCustomHeight(height);
            }
        }
        
        if (color != null) {
            door.setColor(color);
            door.setIsCustomColor(color == Color.CUSTOM);
        }
        
        // Initialize the seller to avoid lazy loading issues
        door.getSeller().getName(); 
        
        Door savedDoor = doorRepository.save(door);
        logger.info("Door with ID {} configured successfully", id);
        return savedDoor;
    }

    /**
     * Adds images to a door.
     * Validates image upload and updates door entity.
     *
     * @param id Door ID to add images to
     * @param images List of images to upload
     * @return Door entity
     * @throws BadRequestException if image upload fails
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @doorSecurityService.isSeller(#id))")
    @CachePut(value = "doors", key = "#id")
    public Door addImages(Long id, List<MultipartFile> images) {
        Door door = getDoor(id);
        List<String> imageUrls = new ArrayList<>();
        
        for (MultipartFile image : images) {
            try {
                String imageUrl = imageStorageService.storeImage(image);
                imageUrls.add(imageUrl);
            } catch (IOException e) {
                logger.error("Failed to store image for door {}: {}", id, e.getMessage());
                throw new BadRequestException("Failed to store image", e);
            }
        }
        
        door.getImages().addAll(imageUrls);
        return doorRepository.save(door);
    }

    /**
     * Deletes images from a door.
     * Validates image deletion and updates door entity.
     *
     * @param id Door ID to delete images from
     * @param imageUrls List of image URLs to delete
     * @return Door entity
     * @throws BadRequestException if image deletion fails
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @doorSecurityService.isSeller(#id))")
    @CachePut(value = "doors", key = "#id")
    public Door deleteImages(Long id, List<String> imageUrls) {
        Door door = getDoor(id);
        door.getImages().removeAll(imageUrls);
        
        // Delete images from S3
        for (String imageUrl : imageUrls) {
            try {
                imageStorageService.deleteImage(imageUrl);
            } catch (Exception e) {
                logger.warn("Failed to delete image from storage: {}", imageUrl);
                throw new BadRequestException("Failed to delete image", e);
            }
        }
        
        return doorRepository.save(door);
    }

    /**
     * Updates images for a door.
     * Validates image update and updates door entity.
     *
     * @param id Door ID to update images for
     * @param deleteUrls List of image URLs to delete
     * @param newImages List of new images to upload
     * @return Door entity
     * @throws BadRequestException if image update fails
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @doorSecurityService.isSeller(#id))")
    @CachePut(value = "doors", key = "#id")
    public Door updateImages(Long id, List<String> deleteUrls, List<MultipartFile> newImages) {
        // First delete old images
        deleteImages(id, deleteUrls);
        
        // Then add new images
        return addImages(id, newImages);
    }

    /**
     * Configures a door's dimensions.
     * Validates dimension update and updates door entity.
     *
     * @param doorId Door ID to configure dimensions for
     * @param customWidth Custom width
     * @param customHeight Custom height
     * @return Door entity
     * @throws BadRequestException if validation fails
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @doorSecurityService.isSeller(#doorId))")
    public Door configureDoorDimensions(Long doorId, Double customWidth, Double customHeight) {
        Door door = getDoor(doorId);
        if (door.getSize() == Size.CUSTOM) {
            door.setCustomWidth(customWidth);
            door.setCustomHeight(customHeight);
            door.calculateFinalPrice();
            doorRepository.save(door);
            logger.info("Configured dimensions for door ID: {}", doorId);
        } else {
            throw new BadRequestException("Only doors with CUSTOM size can have custom dimensions");
        }
        return door;
    }

    /**
     * Retrieves all doors.
     * Internal helper method for door operations.
     *
     * @param pageRequest Page request
     * @return Page of doors
     */
    public Page<Door> getAllDoors(PageRequest pageRequest) {
        return doorRepository.findAll(pageRequest);
    }

    /**
     * Retrieves similar doors.
     * Internal helper method for door operations.
     *
     * @param id Door ID to retrieve similar doors for
     * @param limit Limit of similar doors to retrieve
     * @return List of similar doors
     */
    public List<Door> getSimilarDoors(Long id, int limit) {
        Door door = getDoor(id); 
        
        return doorRepository.findByMaterialAndColorAndPriceBetweenAndIdNot(
            door.getMaterial(),
            door.getColor(),
            door.getPrice() * 0.8, // 20% price range below
            door.getPrice() * 1.2, // 20% price range above
            id,
            PageRequest.of(0, limit)
        );
    }

    /**
     * Uploads images for a door.
     * Validates image files and stores them using the image storage service.
     *
     * @param id Door ID
     * @param images Array of image files to upload
     * @return Updated door with new images
     * @throws EntityNotFoundException if door not found
     * @throws BadRequestException if images are invalid
     * @throws IOException if image upload fails
     */
    @Transactional
    public Door uploadImages(Long id, MultipartFile[] images) throws IOException {
        logger.info("Uploading {} images for door ID: {}", images.length, id);
        
        if (images == null || images.length == 0) {
            throw new BadRequestException("No images provided");
        }

        Door door = getDoor(id);
        List<String> imageUrls = new ArrayList<>();

        // Validate and upload each image
        for (MultipartFile image : images) {
            if (image.isEmpty()) {
                throw new BadRequestException("Empty image file provided");
            }

            String imageUrl = imageStorageService.storeImage(image);
            imageUrls.add(imageUrl);
        }

        // Update door with new image URLs
        if (door.getImages() == null) {
            door.setImages(new ArrayList<>());
        }
        door.getImages().addAll(imageUrls);
        return doorRepository.save(door);
    }

    /**
     * Validates image content type.
     * @param contentType MIME type of the image
     * @return true if valid image type
     */
    private boolean isValidImageType(String contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.equals("image/jpeg") ||
               contentType.equals("image/png") ||
               contentType.equals("image/gif") ||
               contentType.equals("image/webp");
    }
}