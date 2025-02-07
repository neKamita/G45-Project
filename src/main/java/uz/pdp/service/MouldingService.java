package uz.pdp.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import uz.pdp.dto.MouldingCreateDTO;
import uz.pdp.dto.MouldingDTO;
import uz.pdp.entity.Moulding;
import uz.pdp.entity.User;
import uz.pdp.enums.Role;
import uz.pdp.repository.MouldingRepository;
import uz.pdp.repository.UserRepository;
import uz.pdp.payload.*;

/**
 * Service for managing moulding operations.
 * Where all the moulding magic happens! 
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MouldingService {

    private final MouldingRepository mouldingRepository;
    private final UserRepository userRepository;
    @Autowired
    private ImageStorageService imageStorageService;

    private static final Logger logger = LoggerFactory.getLogger(MouldingService.class);

    /**
     * Get all mouldings with pagination.
     * Serving mouldings one page at a time! 
     */
    public Page<MouldingDTO> getAllMouldings(Pageable pageable) {
        return mouldingRepository.findAll(pageable).map(this::toDTO);
    }

    /**
     * Get all mouldings without pagination.
     * The whole collection, because sometimes more is more! 
     */
    public List<MouldingDTO> getAllMouldings() {
        return mouldingRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    /**
     * Get moulding by ID.
     * Finding that one special moulding in the crowd! 
     */
    public Optional<Moulding> getMouldingById(Long id) {
        return mouldingRepository.findById(id);
    }

    /**
     * Finds all mouldings by a specific user.
     * Let's see what masterpieces this craftsperson has created! 
     * 
     * @param user The user to find mouldings for
     * @return A list of mouldings for the user in a custom EntityResponse payload
     * @throws IllegalArgumentException if the user is null or does not exist. Doors need a home! 
     */
    public EntityResponse<List<Moulding>> findByUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null. Your basket is as empty as a doorway without a door!");
        }
        List<Moulding> mouldings = mouldingRepository.findAllByUser(user);

        if (mouldings.isEmpty()) {
            return new EntityResponse<>("No mouldings found for user", true, mouldings);
        }
        return new EntityResponse<>("Mouldings found for user", true, mouldings);
    }

    /**
     * Creates a new moulding from CreateDTO.
     * Time to bring a new moulding into the world! 
     */
    @Transactional
    public Moulding createMoulding(MouldingCreateDTO dto, User seller) {
        // Additional validation
        if (!dto.isValidDimensions()) {
            throw new IllegalArgumentException("Size dimensions must be between 0 and 1000");
        }
        if (!dto.isValidArticle()) {
            throw new IllegalArgumentException("Invalid article format");
        }

        // Check for unique article
        if (mouldingRepository.existsByArticle(dto.getArticle())) {
            throw new IllegalArgumentException("Article already exists");
        }

        Moulding moulding = new Moulding();
        moulding.setUser(seller);
        updateMouldingFromCreateDTO(moulding, dto);
        return mouldingRepository.save(moulding);
    }

    /**
     * Updates an existing moulding from DTO.
     * Because even mouldings need a makeover sometimes! 
     */
    @Transactional
    public Moulding updateMoulding(Long id, MouldingDTO dto, User currentUser) {
        Moulding moulding = mouldingRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Moulding not found"));

        // Verify ownership
        if (!currentUser.getRole().equals(Role.ADMIN) &&
            !currentUser.getId().equals(moulding.getUser().getId())) {
            throw new AccessDeniedException("You don't have permission to update this moulding");
        }

        // Update moulding details
        moulding.setTitle(dto.getTitle());
        moulding.setDescription(dto.getDescription());
        moulding.setPrice(dto.getPrice());
        moulding.setQuantity(dto.getQuantity());
        moulding.setSize(dto.getSize());

        // Save and return updated moulding
        moulding = mouldingRepository.save(moulding);
        return moulding;
    }

    /**
     * Uploads a moulding image to S3 storage.
     * Because every moulding deserves its glamour shot! 
     *
     * @param file Image file to upload
     * @return URL of the uploaded image
     * @throws IllegalArgumentException if file is invalid
     * @throws IOException if upload fails
     */
    public String uploadImage(MultipartFile file) throws IOException {
        try {
            return imageStorageService.storeMouldingImage(file);
        } catch (Exception e) {
            logger.error("Failed to upload moulding image: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Updates images for a moulding.
     * Because every moulding deserves a glamorous photoshoot! 
     *
     * @param id The ID of the moulding to update
     * @param deleteUrls List of image URLs to delete
     * @param newImages Array of new images to upload
     * @param currentUser The user performing the update
     * @return Updated MouldingDTO
     * @throws IllegalArgumentException if request is invalid
     * @throws IllegalStateException if user is not authorized
     */
    @Transactional
    public MouldingDTO updateMouldingImages(Long id, List<String> deleteUrls, 
            MultipartFile[] newImages, User currentUser) {
        log.info("Starting image update for moulding id={}", id);
                
        // Get current moulding
        Moulding moulding = getMouldingById(id)
            .orElseThrow(() -> new IllegalArgumentException("Moulding not found"));

        log.info("Found moulding with current images: {}", moulding.getImagesUrl());

        // Verify ownership
        if (!currentUser.getRole().equals(Role.ADMIN) && 
            !currentUser.getId().equals(moulding.getUser().getId())) {
            throw new AccessDeniedException("You don't have permission to update this moulding's images");
        }

        // Initialize current images list safely with validation
        List<String> currentImages = moulding.getImagesUrl();
        if (currentImages == null) {
            currentImages = new ArrayList<>();
        }
        log.info("Validated current images list: {}", currentImages);

        // Handle image deletion
        boolean anyChanges = false;
        if (deleteUrls != null && !deleteUrls.isEmpty()) {
            log.info("Processing {} URLs for deletion: {}", deleteUrls.size(), deleteUrls);
            
            for (String url : deleteUrls) {
                try {
                    // Skip invalid URLs
                    if (!isValidS3Url(url)) {
                        throw new IllegalArgumentException("Invalid S3 URL format: " + url);
                    }

                    log.info("Processing delete URL: {}", url);

                    // Check if URL exists in current images
                    if (!currentImages.contains(url)) {
                        throw new IllegalArgumentException("Image not found: " + url + 
                            ". It may have been already deleted.");
                    }

                    // Delete from S3
                    log.info("Attempting to delete image from S3: {}", url);
                    imageStorageService.deleteImage(url);
                    
                    // Remove from current images list
                    currentImages.remove(url);
                    anyChanges = true;
                    log.info("Successfully deleted image. Remaining images: {}", currentImages);
                    
                } catch (IllegalArgumentException e) {
                    // Throw validation errors to user
                    throw e;
                } catch (Exception e) {
                    log.error("Failed to delete image {}: {}", url, e.getMessage(), e);
                    throw new IllegalStateException("Failed to delete image: " + e.getMessage());
                }
            }
            
            // Update moulding's image list after deletions if any changes were made
            if (anyChanges) {
                moulding.setImagesUrl(currentImages);
                mouldingRepository.save(moulding);
                log.info("Updated moulding after deletions. Current images: {}", currentImages);
            }
        }

        // Handle new images if provided
        List<String> newImageUrls = new ArrayList<>();
        if (newImages != null && newImages.length > 0) {
            log.info("Processing {} new images", newImages.length);
            
            for (MultipartFile image : newImages) {
                if (image == null || image.isEmpty()) {
                    log.warn("Skipping empty file in newImages");
                    continue;
                }
                try {
                    String imageUrl = imageStorageService.storeMouldingImage(image);
                    log.info("Uploaded new image: {}", imageUrl);
                    
                    if (isValidS3Url(imageUrl)) {
                        newImageUrls.add(imageUrl);
                        anyChanges = true;
                        log.info("Added new image URL to list");
                    } else {
                        log.error("Invalid URL format returned from S3: {}", imageUrl);
                        throw new IllegalStateException("Failed to upload image: invalid URL format");
                    }
                } catch (Exception e) {
                    log.error("Failed to upload image {}: {}", image.getOriginalFilename(), e.getMessage(), e);
                    throw new IllegalStateException("Failed to upload image: " + e.getMessage());
                }
            }
        }

        try {
            // Only update if there were any changes
            if (anyChanges) {
                // Update moulding with final image list
                currentImages.addAll(newImageUrls);
                moulding.setImagesUrl(currentImages);
                Moulding updatedMoulding = mouldingRepository.save(moulding);
                log.info("Successfully updated moulding with final image list: {}", currentImages);
                return toDTO(updatedMoulding);
            } else {
                // No changes were made
                log.info("No changes were made to the images list");
                return toDTO(moulding);
            }
        } catch (Exception e) {
            log.error("Failed to update moulding with new images: {}", e.getMessage(), e);
            
            // Cleanup any newly uploaded images
            for (String url : newImageUrls) {
                try {
                    imageStorageService.deleteImage(url);
                } catch (Exception ex) {
                    log.warn("Failed to cleanup image {} after failed update: {}", url, ex.getMessage());
                }
            }
            throw new IllegalStateException("Failed to update moulding: " + e.getMessage());
        }
    }

    /**
     * Deletes a moulding by ID.
     * Goodbye, old friend! 
     */
    public void deleteMoulding(Long id, User currentUser) {
        Moulding moulding = mouldingRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Moulding not found"));

        if (!currentUser.getRole().equals(Role.ADMIN) && 
            !currentUser.getId().equals(moulding.getUser().getId())) {
            throw new AccessDeniedException("You don't have permission to delete this moulding");
        }

        mouldingRepository.delete(moulding);
    }

    /**
     * Converts Moulding to DTO.
     * Transforming data like a boss! 
     */
    public MouldingDTO toDTO(Moulding moulding) {
        MouldingDTO dto = new MouldingDTO();
        dto.setId(moulding.getId());
        dto.setTitle(moulding.getTitle());
        dto.setSize(moulding.getSize());
        dto.setArticle(moulding.getArticle());
        dto.setPrice(moulding.getPrice());
        dto.setQuantity(moulding.getQuantity());
        dto.setDescription(moulding.getDescription());
        dto.setImagesUrl(moulding.getImagesUrl());
        
        // Safely handle null user
        User user = moulding.getUser();
        if (user != null) {
            dto.setSellerId(user.getId());
        }
        
        return dto;
    }

    private void updateMouldingFromDTO(Moulding moulding, MouldingDTO dto) {
        // Validate title
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (dto.getTitle().length() > 100) {
            throw new IllegalArgumentException("Title cannot be longer than 100 characters");
        }

        // Validate size format (e.g., "75x38" or 75x38)
        if (dto.getSize() == null) {
            throw new IllegalArgumentException("Size cannot be empty");
        }
        // Convert to string and remove any quotes
        String sizeStr = dto.getSize().toString().replace("\"", "").trim();
        if (!sizeStr.matches("^\\d+(\\.\\d+)?x\\d+(\\.\\d+)?$")) {
            throw new IllegalArgumentException("Invalid size format. Must be in format: widthxheight (e.g., 75x38 or 75.5x38.5)");
        }
        // Check size values are reasonable
        String[] dimensions = sizeStr.split("x");
        double width = Double.parseDouble(dimensions[0]);
        double height = Double.parseDouble(dimensions[1]);
        if (width <= 0 || width > 1000 || height <= 0 || height > 1000) {
            throw new IllegalArgumentException("Size dimensions must be between 0 and 1000");
        }

        // Validate article
        if (dto.getArticle() == null || dto.getArticle().trim().isEmpty()) {
            throw new IllegalArgumentException("Article cannot be empty");
        }
        if (!dto.getArticle().matches("^[A-Za-z0-9-]+$")) {
            throw new IllegalArgumentException("Article must contain only letters, numbers, and hyphens");
        }
        if (dto.getArticle().length() > 50) {
            throw new IllegalArgumentException("Article cannot be longer than 50 characters");
        }

        // Validate price
        if (dto.getPrice() == null) {
            throw new IllegalArgumentException("Price cannot be null");
        }
        if (dto.getPrice() < 0 || dto.getPrice() > 999999.99) {
            throw new IllegalArgumentException("Price must be between 0 and 999999.99");
        }

        // Validate quantity
        if (dto.getQuantity() == null) {
            throw new IllegalArgumentException("Quantity cannot be null");
        }
        if (dto.getQuantity() < 0 || dto.getQuantity() > 999999) {
            throw new IllegalArgumentException("Quantity must be between 0 and 999999");
        }

        // Validate description (optional)
        if (dto.getDescription() != null && dto.getDescription().length() > 1000) {
            throw new IllegalArgumentException("Description cannot be longer than 1000 characters");
        }

        // Set the validated values
        moulding.setTitle(dto.getTitle().trim());
        moulding.setSize(sizeStr); // Use the cleaned size string
        moulding.setArticle(dto.getArticle().trim());
        moulding.setPrice(dto.getPrice());
        moulding.setQuantity(dto.getQuantity());
        moulding.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        
        // Only update images if provided
        if (dto.getImagesUrl() != null) {
            moulding.setImagesUrl(dto.getImagesUrl());
        }

        // Calculate price overall (always calculated, not taken from DTO)
        moulding.setPriceOverall(moulding.getPrice() * moulding.getQuantity());
    }

    private void updateMouldingFromCreateDTO(Moulding moulding, MouldingCreateDTO dto) {
        moulding.setTitle(dto.getTitle().trim());
        moulding.setSize(dto.getSize().trim());
        moulding.setArticle(dto.getArticle().trim());
        moulding.setPrice(dto.getPrice());
        moulding.setQuantity(dto.getQuantity());
        moulding.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        
        // Set images if provided
        if (dto.getImagesUrl() != null) {
            moulding.setImagesUrl(dto.getImagesUrl());
        }

        // Calculate price overall
        moulding.setPriceOverall(dto.getPrice() * dto.getQuantity());
    }

    /**
     * Validates if a user has permission to modify a moulding.
     * Because even doors need their bouncers! 
     *
     * @param moulding The moulding to check
     * @param user The user to validate
     * @return true if user is authorized
     */
    private boolean isSeller(Moulding moulding, User user) {
        return user.getRole().equals(Role.ADMIN) || 
               user.getId().equals(moulding.getUser().getId());
    }

    /**
     * Validates if a URL is a valid S3 moulding image URL
     * Because even URLs need a proper dress code! 
     */
    private boolean isValidS3Url(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        // Check basic URL structure
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            return false;
        }
        
        // Check if it's our mouldings path
        return url.contains("/mouldings/");
    }
}
