package uz.pdp.service;

import java.util.List;
import java.util.ArrayList;

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
import uz.pdp.repository.DoorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.SneakyThrows;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class DoorService {
    private static final Logger logger = LoggerFactory.getLogger(DoorService.class);

    @Autowired
    private DoorRepository doorRepository;
    
    @Autowired
    private UserService userService;

    @Autowired
    private ImageStorageService imageStorageService;

    // Open to all users - no @PreAuthorize needed
    @Transactional(readOnly = true)
    public Door getDoor(Long id) {
        return doorRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Door not found with id: " + id));
    }

    // Open to all users - no @PreAuthorize needed
    @Transactional(readOnly = true)
    public Page<Door> getAllDoors(int page, int size) {
        return doorRepository.findAll(PageRequest.of(page, size));
    }
@Transactional
@PreAuthorize("hasRole('ADMIN') or hasRole('SELLER')")
@CachePut(value = "doors", key = "#result.id")
public Door createDoor(DoorDto doorDto) {
    User currentUser = userService.getCurrentUser();
    Door door = new Door();
    mapDtoToEntity(doorDto, door);
    door.setSeller(currentUser);
    door.calculateFinalPrice();
    Door savedDoor = doorRepository.saveAndFlush(door);
    logger.info("Door created with ID: {}", savedDoor.getId());
    return savedDoor;
}

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

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @doorSecurityService.isSeller(#id))")
    @CachePut(value = "doors", key = "#id")
    public Door updateDoor(Long id, DoorDto doorDto) {
        logger.info("Updating door with ID: {}, data: {}", id, doorDto);
        Door door = getDoor(id);
        mapDtoToEntity(doorDto, door);
        door.calculateFinalPrice();
        Door savedDoor = doorRepository.save(door);
        logger.info("Door with ID {} updated.", id);
        return savedDoor;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @doorSecurityService.isSeller(#id))")
    @CacheEvict(value = {"doors", "allDoors"}, allEntries = true)
    public void deleteDoor(Long id) {
        logger.info("Deleting door with ID: {}", id);
        Door door = getDoor(id);
        doorRepository.delete(door);
        logger.info("Door with ID {} deleted.", id);
    }

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
                    throw new Exception(
                        "Custom size requires both width and height to be specified");
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
                throw new RuntimeException("Failed to store image", e);
            }
        }
        
        door.getImages().addAll(imageUrls);
        return doorRepository.save(door);
    }

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
            }
        }
        
        return doorRepository.save(door);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or (hasRole('SELLER') and @doorSecurityService.isSeller(#id))")
    @CachePut(value = "doors", key = "#id")
    public Door updateImages(Long id, List<String> deleteUrls, List<MultipartFile> newImages) {
        // First delete old images
        deleteImages(id, deleteUrls);
        
        // Then add new images
        return addImages(id, newImages);
    }

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
            throw new IllegalArgumentException("Only doors with CUSTOM size can have custom dimensions");
        }
        return door;
    }

    public Page<Door> getAllDoors(PageRequest pageRequest) {
        return doorRepository.findAll(pageRequest);
    }

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
}