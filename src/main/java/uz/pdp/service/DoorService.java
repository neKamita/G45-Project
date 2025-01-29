package uz.pdp.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import lombok.SneakyThrows;
import uz.pdp.dto.DoorDto;
import uz.pdp.entity.Category;
import uz.pdp.entity.Door;
import uz.pdp.entity.User;
import uz.pdp.enums.*;
import uz.pdp.exception.*;
import uz.pdp.payload.EntityResponse;
import uz.pdp.repository.CategoryRepository;
import uz.pdp.repository.DoorHistoryRepository;
import uz.pdp.repository.DoorRepository;
import uz.pdp.repository.UserRepository;

/**
 * Service class for managing door operations.
 * Now with turbocharged Redis caching! 
 * Because the only thing faster than our doors is our data access.
 *
 * @version 1.0
 * @since 2025-01-17
 */
@Service
public class DoorService {
    private static final Logger logger = LoggerFactory.getLogger(DoorService.class);
    private static final String DOORS_CACHE = "doors";
    private static final String DOOR_CACHE = "door";
    private static final String DOOR_COLORS_CACHE = "door-colors";
    private static final String DOOR_VARIANTS_CACHE = "door-variants";

    private final DoorRepository doorRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final ImageStorageService imageStorageService;
    private final DoorHistoryRepository doorHistoryRepository;

    @Autowired
    public DoorService(DoorRepository doorRepository, CategoryRepository categoryRepository, UserRepository userRepository, UserService userService, ImageStorageService imageStorageService,
                       DoorHistoryRepository doorHistoryRepository) {
        this.doorRepository = doorRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.imageStorageService = imageStorageService;
        this.doorHistoryRepository = doorHistoryRepository;
    }

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
    @Cacheable(value = DOOR_CACHE, key = "#id")
    public Door getDoor(Long id) {
        return doorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Door not found with id: " + id));
    }

    /**
     * Retrieves all doors accessible to the current user.
     * Admins can see all doors, while regular users only see doors they have access to.
     *
     * @return List of accessible doors
     */
    // Open to all users - no @PreAuthorize needed
    @Transactional(readOnly = true)
    @Cacheable(value = DOORS_CACHE, key = "'doors:page:' + #page + ':size:' + #size", unless = "#result == null")
    public List<Door> getAllDoors(int page, int size) {
        try {
            logger.debug("🚪 Fetching doors from page {} with size {}", page, size);
            Page<Door> doors = doorRepository.findAll(PageRequest.of(page, size));
            
            // Initialize lazy collections to avoid serialization issues
            List<Door> content = doors.getContent();
            content.forEach(door -> {
                if (door.getImages() != null) {
                    door.getImages().size(); // Initialize images collection
                }
                if (door.getColor() != null) {
                    door.getColor().name(); // Initialize enum
                }
            });
            
            return new ArrayList<>(content);
        } catch (Exception e) {
            logger.error("❌ Failed to fetch doors: {}", e.getMessage());
            throw new ServiceException("Failed to fetch doors", e);
        }
    }

    /**
     * Creates a new door in the system. 
     * This is where baby doors come from.
     *
     * @param doorDto The blueprint for our new door (please make it a good one)
     * @return A beautiful new Door object, ready to be opened and closed
     * @throws BadRequestException if you try to create a door in a black hole
     * @throws ConflictException if you try to create a door where another door already exists (that's just rude)
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
    @CacheEvict(value = DOORS_CACHE, allEntries = true)
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
        
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));
            door.setCategory(category);
        }
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
    @Caching(put = {
        @CachePut(value = DOOR_CACHE, key = "#id")
    }, evict = {
        @CacheEvict(value = DOORS_CACHE, allEntries = true),
        @CacheEvict(value = DOOR_COLORS_CACHE, key = "#id"),
        @CacheEvict(value = DOOR_VARIANTS_CACHE, key = "#id")
    })
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
     * Deletes a door from existence. 
     * Press F to pay respects.
     *
     * @param id The ID of the door we're sending to the shadow realm
     * @throws EntityNotFoundException if the door already ascended to a higher plane of existence
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = DOOR_CACHE, key = "#id"),
        @CacheEvict(value = DOORS_CACHE, allEntries = true),
        @CacheEvict(value = DOOR_COLORS_CACHE, key = "#id"),
        @CacheEvict(value = DOOR_VARIANTS_CACHE, key = "#id")
    })
    public EntityResponse<Void> deleteDoor(Long id) {
        try {
            logger.info("Deleting door with ID: {}", id);
            Door door = getDoorById(id);

            // Delete associated images
            if (door.getImages() != null && !door.getImages().isEmpty()) {
                for (String imageUrl : door.getImages()) {
                    try {
                        imageStorageService.deleteImage(imageUrl);
                    } catch (Exception e) {
                        logger.warn("Failed to delete image from storage: {}", imageUrl);
                    }
                }
            }

            // Delete associated history records
            doorHistoryRepository.deleteByDoorId(id);

            // Delete the door
            doorRepository.delete(door);
            logger.info("Successfully deleted door with ID: {}", id);
            return EntityResponse.success("Door deleted successfully");
        } catch (ResourceNotFoundException e) {
            logger.error("Door not found - ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting door {}: {}", id, e.getMessage());
            throw new BadRequestException("Failed to delete door: " + e.getMessage());
        }
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
    @CachePut(value = DOOR_CACHE, key = "#id")
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
    @CachePut(value = DOOR_CACHE, key = "#id")
    public Door addImages(Long id, List<MultipartFile> images) {
        Door door = getDoor(id);
        List<String> imageUrls = new ArrayList<>();
        
        for (MultipartFile image : images) {
            try {
                String imageUrl = imageStorageService.storeDoorImage(image);
                imageUrls.add(imageUrl);
            } catch (IOException e) {
                logger.error("Failed to store image for door {}: {}", id, e.getMessage());
                throw new BadRequestException("Failed to store image", e.getMessage(), e);
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
    @CachePut(value = DOOR_CACHE, key = "#id")
    public Door deleteImages(Long id, List<String> imageUrls) {
        Door door = getDoor(id);
        door.getImages().removeAll(imageUrls);
        
        // Delete images from storage
        for (String imageUrl : imageUrls) {
            try {
                imageStorageService.deleteImage(imageUrl);
            } catch (Exception e) {
                logger.warn("Failed to delete image from storage: {}", imageUrl);
                throw new BadRequestException("Failed to delete image", e.getMessage(), e);
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
    @CachePut(value = DOOR_CACHE, key = "#id")
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
    @CachePut(value = DOOR_CACHE, key = "#doorId")
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
     * @return List of doors
     */
    public List<Door> getAllDoors(PageRequest pageRequest) {
        return doorRepository.findAll(pageRequest).getContent();
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
     * Handles image upload for a door.
     * Validates image format and stores in configured location.
     *
     * @param id Door ID to update
     * @param images List of image files
     * @return Door entity with updated images
     * @throws BadRequestException if validation fails
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @doorSecurityService.isSeller(#id))")
    //@CachePut(value = DOOR_CACHE, key = "#id")
    public Door uploadImages(Long id, List<MultipartFile> images) {
        Door door = getDoor(id);
        List<String> imageUrls = new ArrayList<>();

        try {
            for (MultipartFile image : images) {
                String imageUrl = imageStorageService.storeDoorImage(image);
                imageUrls.add(imageUrl);
            }
            door.setImages(imageUrls);
            return doorRepository.save(door);
        } catch (IOException e) {
            logger.error("Failed to upload images for door {}: {}", id, e.getMessage());
            throw new BadRequestException("Failed to upload images: " + e.getMessage());
        }
    }

    /**
     * Updates door status.
     * Validates status transition and updates door entity.
     *
     * @param id Door ID to update
     * @param status New door status
     * @return Door entity
     * @throws BadRequestException if status transition is invalid
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @doorSecurityService.isSeller(#id))")

    public Door updateStatus(Long id, DoorStatus status) {
        try {
            Door door = getDoor(id);
            door.setStatus(status);
            return doorRepository.save(door);
        } catch (Exception e) {
            logger.error("Failed to update status for door {}: {}", id, e.getMessage());
            throw new BadRequestException("Failed to update door status: " + e.getMessage());
        }
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

    /**
     * Retrieves all doors in the system.
     * Only accessible by ADMIN users.
     *
     * @return List of all doors
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<Door> getAllDoors() {
        try {
            logger.info("Retrieving all doors");
            List<Door> doors = doorRepository.findAll();
            logger.info("Retrieved {} doors", doors.size());
            return doors;
        } catch (Exception e) {
            logger.error("Error retrieving all doors: {}", e.getMessage());
            throw new BadRequestException("Failed to retrieve doors: " + e.getMessage());
        }
    }

    /**
     * Retrieves a door by its ID.
     *
     * @param id ID of the door to retrieve
     * @return Door if found
     * @throws ResourceNotFoundException if door not found
     */
    //@Cacheable(value = DOOR_CACHE, key = "#id")
    public Door getDoorById(Long id) {
        try {
            logger.info("Retrieving door with ID: {}", id);
            Door door = doorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Door not found"));
            logger.info("Retrieved door: {}", door.getName());
            return door;
        } catch (ResourceNotFoundException e) {
            logger.error("Door not found - ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving door {}: {}", id, e.getMessage());
            throw new BadRequestException("Failed to retrieve door: " + e.getMessage());
        }
    }

    /**
     * Searches for doors based on search criteria.
     *
     * @param searchTerm Term to search for in door properties
     * @return List of matching doors
     */
    public List<Door> searchDoors(String searchTerm) {
        try {
            logger.info("Searching doors with term: {}", searchTerm);
            List<Door> doors = doorRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                searchTerm, searchTerm);
            logger.info("Found {} matching doors", doors.size());
            return doors;
        } catch (Exception e) {
            logger.error("Error searching doors: {}", e.getMessage());
            throw new BadRequestException("Failed to search doors: " + e.getMessage());
        }
    }

    /**
     * Creates a new door.
     * Requires seller or admin privileges.
     *
     * @param door Door entity to create
     * @return EntityResponse containing created door
     */
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Transactional
    //@CacheEvict(value = DOORS_CACHE, allEntries = true)
    public EntityResponse<Door> createDoor(Door door) {
        try {
            logger.info("Creating new door");
            User currentUser = userService.getCurrentUser();
            door.setSeller(currentUser);
            
            // Set default status values
            door.setStatus(DoorStatus.PENDING);
            door.setActive(true);
            
            Door savedDoor = doorRepository.save(door);
            logger.info("Created door with ID: {}", savedDoor.getId());
            return EntityResponse.success("Door created successfully", savedDoor);
        } catch (Exception e) {
            logger.error("Error creating door: {}", e.getMessage());
            throw new BadRequestException("Failed to create door: " + e.getMessage());
        }
    }

    /**
     * Updates an existing door.
     * Only the door's seller or an admin can update it.
     *
     * @param id ID of the door to update
     * @param updatedDoor Updated door details
     * @return EntityResponse containing updated door
     */
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Transactional

    public EntityResponse<Door> updateDoor(Long id, Door updatedDoor) {
        try {
            logger.info("Updating door with ID: {}", id);
            Door existingDoor = getDoorById(id);
            User currentUser = userService.getCurrentUser();
            
            // Check if user has permission to update
            if (!currentUser.getRole().equals(Role.ADMIN) &&
                !existingDoor.getSeller().getId().equals(currentUser.getId())) {
                throw new BadRequestException("You don't have permission to update this door");
            }
            
            // Update fields
            existingDoor.setName(updatedDoor.getName());
            existingDoor.setDescription(updatedDoor.getDescription());
            existingDoor.setPrice(updatedDoor.getPrice());
            existingDoor.setSize(updatedDoor.getSize());
            existingDoor.setColor(updatedDoor.getColor());
            existingDoor.setMaterial(updatedDoor.getMaterial());
            
            Door savedDoor = doorRepository.save(existingDoor);
            logger.info("Updated door with ID: {}", savedDoor.getId());
            return EntityResponse.success("Door updated successfully", savedDoor);
        } catch (Exception e) {
            logger.error("Error updating door {}: {}", id, e.getMessage());
            throw new BadRequestException("Failed to update door: " + e.getMessage());
        }
    }

    /**
     * Updates door status.
     * Only the door's seller or an admin can update status.
     *
     * @param doorId ID of the door
     * @param isAvailable If true, sets status to AVAILABLE, if false sets to UNAVAILABLE
     * @param isActive New active status
     * @return Updated door
     */
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    @Transactional

    public Door updateDoorStatus(Long doorId, Boolean isAvailable, Boolean isActive) {
        try {
            logger.info("Updating status for door ID: {}", doorId);
            Door door = getDoorById(doorId);
            User currentUser = userService.getCurrentUser();
            
            // Check if user has permission to update
            if (!currentUser.getRole().equals(Role.ADMIN) && 
                !door.getSeller().getId().equals(currentUser.getId())) {
                throw new BadRequestException("You don't have permission to update this door's status");
            }
            
            if (isAvailable != null) {
                door.setStatus(isAvailable ? DoorStatus.AVAILABLE : DoorStatus.UNAVAILABLE);
            }
            if (isActive != null) {
                door.setActive(isActive);
            }
            
            Door savedDoor = doorRepository.save(door);
            logger.info("Updated status for door ID: {}", doorId);
            return savedDoor;
        } catch (Exception e) {
            logger.error("Error updating door status {}: {}", doorId, e.getMessage());
            throw new BadRequestException("Failed to update door status: " + e.getMessage());
        }
    }

    /**
     * Finds all doors with a specific color.
     * Because sometimes you just need to match your door to your mood! 
     *
     * @param color The color to filter doors by
     * @return List of doors in that color, or an empty list if no doors are feeling that color today
     */
    @Transactional(readOnly = true)
   // @Cacheable(value = DOOR_COLORS_CACHE, key = "#color")
    public List<Door> getDoorsByColor(Color color) {
        logger.info("Searching for doors with color: {}", color);
        return doorRepository.findByColorAndActiveTrue(color);
    }

    /**
     * Gets all color variants of a door, including the base model.
     * Like a door's extended family reunion! 
     *
     * @param doorId ID of any variant or base model
     * @return List of all color variants
     */
    @Transactional(readOnly = true)
    //@Cacheable(value = DOOR_VARIANTS_CACHE, key = "#doorId")
    public List<Door> getDoorColorVariants(Long doorId) {
        Door door = getDoor(doorId);
        Long baseModelId = door.getIsBaseModel() ? door.getId() : door.getBaseModelId();
        
        if (baseModelId == null) {
            return Collections.singletonList(door);
        }
        
        return doorRepository.findByBaseModelIdOrId(baseModelId, baseModelId);
    }

    /**
     * Creates a color variant of an existing door.
     * It's like giving a door a new paint job, but fancier! 
     *
     * @param doorId Base door ID to create variant from
     * @param color New color for the variant
     * @return The newly created door variant
     */
    @Transactional
    @PreAuthorize("hasRole('SELLER')")

    public Door createColorVariant(Long doorId, Color color) {
        Door baseDoor = getDoor(doorId);
        
        // If this is first variant, mark the original as base model
        if (!baseDoor.getIsBaseModel() && baseDoor.getBaseModelId() == null) {
            baseDoor.setIsBaseModel(true);
            baseDoor.getAvailableColors().add(baseDoor.getColor());
            doorRepository.save(baseDoor);
        }
        
        // Create new variant
        Door variant = new Door();
        // Copy all properties except ID and color
        BeanUtils.copyProperties(baseDoor, variant, "id", "color", "images");
        variant.setColor(color);
        variant.setBaseModelId(baseDoor.getIsBaseModel() ? baseDoor.getId() : baseDoor.getBaseModelId());
        variant.setIsBaseModel(false);
        
        // Update available colors on base model
        Door baseModel = baseDoor.getIsBaseModel() ? baseDoor : getDoor(baseDoor.getBaseModelId());
        baseModel.getAvailableColors().add(color);
        doorRepository.save(baseModel);
        
        return doorRepository.save(variant);
    }

    /**
     * Creates a custom colored variant of a door.
     * For when standard colors just won't cut it! 
     *
     * @param doorId Base door ID
     * @param colorCode Hex color code (e.g., #FF5733)
     * @return The custom colored door variant
     */
    @Transactional
    @PreAuthorize("hasRole('SELLER')")

    public Door createCustomColorVariant(Long doorId, String colorCode) {
        Door baseDoor = getDoor(doorId);
        Door baseModel = baseDoor.getIsBaseModel() ? baseDoor : getDoor(baseDoor.getBaseModelId());
        
        Door variant = new Door();
        BeanUtils.copyProperties(baseDoor, variant, "id", "color", "images");
        variant.setCustomColorCode(colorCode);
        variant.setIsCustomColor(true);
        variant.setBaseModelId(baseModel.getId());
        variant.setIsBaseModel(false);
        
        return doorRepository.save(variant);
    }

    /**
     * Gets all available colors for a door model.
     * The door's color palette, if you will! 
     *
     * @param doorId ID of any variant or base model
     * @return Set of available colors
     */
    @Transactional(readOnly = true)
   // @Cacheable(value = DOOR_COLORS_CACHE, key = "#doorId")
    public Set<Color> getAvailableColors(Long doorId) {
        Door door = getDoor(doorId);
        Long baseModelId = door.getIsBaseModel() ? door.getId() : door.getBaseModelId();
        
        if (baseModelId == null) {
            return Collections.singleton(door.getColor());
        }
        
        Door baseModel = getDoor(baseModelId);
        return baseModel.getAvailableColors();
    }

    /**
     * Get available colors for a door model.
     * If you thought choosing a Netflix show was hard, 
     * wait until you see our color selection! 
     * 
     * @param id Door ID to get colors for
     * @return Set of available colors for the door
     * @throws ResourceNotFoundException if door not found
     */
    @Transactional(readOnly = true)
    //@Cacheable(value = DOOR_COLORS_CACHE, key = "#id")
    public Set<Color> getDoorColors(Long id) {
        Door door = getDoor(id);
        
        // If this is a variant, get colors from base model
        if (!door.getIsBaseModel() && door.getBaseModelId() != null) {
            door = getDoor(door.getBaseModelId());
        }
        
        // If no colors are set, return an empty set
        if (door.getAvailableColors() == null) {
            logger.warn("Door {} has no available colors set!  This door is having an identity crisis.", id);
            return Collections.emptySet();
        }
        
        logger.info("Found {} fabulous colors for door {}! ", 
            door.getAvailableColors().size(), door.getName());
        return door.getAvailableColors();
    }
    
    private DoorDto mapToDto(Door door) {
        DoorDto doorDto = new DoorDto();
        doorDto.setId(door.getId());
        
        if (door.getCategory() != null) {
            doorDto.setCategoryName(door.getCategory().getName());  // Only set categoryName for responses
        }
        
        doorDto.setName(door.getName());
        doorDto.setDescription(door.getDescription());
        doorDto.setPrice(door.getPrice());
        doorDto.setFinalPrice(door.getFinalPrice());
        doorDto.setSize(door.getSize());
        doorDto.setColor(door.getColor());
        doorDto.setMaterial(door.getMaterial());
        doorDto.setManufacturer(door.getManufacturer());
        doorDto.setFrameType(door.getFrameType());
        doorDto.setHardware(door.getHardware());
        doorDto.setDoorLocation(door.getDoorLocation());
        doorDto.setWarrantyYears(door.getWarrantyYears());
        doorDto.setCustomWidth(door.getCustomWidth());
        doorDto.setCustomHeight(door.getCustomHeight());
        doorDto.setIsCustomColor(door.getIsCustomColor());
        doorDto.setImages(door.getImages());
        doorDto.setStatus(door.getStatus().toString());
        return doorDto;
    }

    /**
     * Get all doors in a specific category.
     * Because every door needs a family! 🏠
     *
     * @param categoryId ID of the category to fetch doors from
     * @return List of doors in the category
     * @throws EntityNotFoundException if category not found
     */
    public List<Door> getDoorsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found: " + categoryId));

        logger.info("Fetching doors for category: {} ({})", category.getName(), categoryId);
        List<Door> doors = doorRepository.findByCategory(category);
        logger.info("Found {} doors in category {}", doors.size(), category.getName());
        
        return doors;
    }
}