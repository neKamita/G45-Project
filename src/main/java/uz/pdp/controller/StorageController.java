package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.dto.StorageDTO;
import uz.pdp.entity.Storage;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.StorageService;

import java.util.List;

/**
 * REST controller for managing product storage locations.
 * Provides endpoints for creating, updating, retrieving, and deleting storage information.
 * Most operations require ADMIN authorization.
 * 
 * Fun fact: This controller is like a GPS for our products! 📍🏭
 *
 * @version 1.0
 * @since 2025-02-10
 */
@RestController
@RequestMapping("/api/storages")
@Hidden
@Tag(name = "Storage Management", description = "APIs for managing product storage locations")
public class StorageController {
    private static final Logger logger = LoggerFactory.getLogger(StorageController.class);
    private final StorageService storageService;

    @Autowired
    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Retrieves all storage locations.
     * A bird's eye view of our product storage empire! 📦
     *
     * @return EntityResponse containing a list of storage locations
     */
    @GetMapping
    @Operation(summary = "Get all storage locations")
    public EntityResponse<List<Storage>> getAllStorages() {
        logger.info("Fetching all storage locations");
        return storageService.getAllStoragesResponse();
    }

    /**
     * Retrieves storage details by ID.
     * Finding that one special storage in our network! 🔍
     *
     * @param id Storage ID
     * @return EntityResponse containing storage details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get storage location by ID")
    public EntityResponse<Storage> getStorage(@PathVariable Long id) {
        logger.info("Fetching storage location with id: {}", id);
        return storageService.getStorageResponse(id);
    }

    /**
     * Adds a new storage location.
     * Building a new home for our precious products! 🏗️
     *
     * @param storageDTO Storage DTO
     * @return EntityResponse containing added storage details
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a new storage location")
    public EntityResponse<Storage> addStorage(@Valid @RequestBody StorageDTO storageDTO) {
        logger.info("Adding new storage location: {}", storageDTO);
        return storageService.addStorageResponse(storageDTO);
    }

    /**
     * Updates an existing storage location.
     * Time for some storage renovation! 🎨
     *
     * @param id Storage ID
     * @param storageDTO Storage DTO
     * @return EntityResponse containing updated storage details
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing storage location")
    public EntityResponse<Storage> updateStorage(
            @PathVariable Long id,
            @Valid @RequestBody StorageDTO storageDTO) {
        logger.info("Updating storage location with id {}: {}", id, storageDTO);
        return storageService.updateStorageResponse(id, storageDTO);
    }

    /**
     * Deletes a storage location.
     * Time to say goodbye to this storage spot! 👋
     *
     * @param id Storage ID
     * @return EntityResponse containing deletion result
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a storage location")
    public EntityResponse<Void> deleteStorage(@PathVariable Long id) {
        logger.info("Deleting storage location with id: {}", id);
        return storageService.deleteStorageResponse(id);
    }

    /**
     * Finds the nearest storage location.
     * Like a storage-seeking compass! 🧭
     *
     * @param latitude Latitude
     * @param longitude Longitude
     * @return EntityResponse containing the nearest storage location
     */
    @GetMapping("/nearest")
    @Operation(summary = "Find nearest storage location")
    public EntityResponse<Storage> findNearestStorage(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        logger.info("Finding nearest storage to coordinates: {}, {}", latitude, longitude);
        return storageService.findNearestStorageResponse(latitude, longitude);
    }
}
