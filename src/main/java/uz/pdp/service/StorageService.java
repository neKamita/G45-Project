package uz.pdp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.dto.StorageDTO;
import uz.pdp.entity.Location;
import uz.pdp.entity.Storage;
import uz.pdp.exception.GlobalExceptionHandler;
import uz.pdp.exception.ResourceNotFoundException;
import uz.pdp.payload.EntityResponse;
import uz.pdp.repository.StorageRepository;

import java.util.List;

/**
 * Service for managing storage locations.
 * The brains behind our storage operations! 
 */
@Service
public class StorageService {

    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);
    private final StorageRepository storageRepository;

    @Autowired
    public StorageService(StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    /**
     * Retrieves all storage locations.
     */
    public EntityResponse<List<Storage>> getAllStoragesResponse() {
        List<Storage> storages = storageRepository.findAll();
        if (storages.isEmpty()) {
            return new EntityResponse<>("No storage locations found in the system", true, storages);
        }
        return new EntityResponse<>("Successfully retrieved " + storages.size() + " storage locations", true, storages);
    }

    /**
     * Retrieves a storage location by ID.
     */
    public Storage getStorage(Long id) {
        return storageRepository.findById(id)
                .orElseThrow(() -> new GlobalExceptionHandler.StorageNotFoundException(id));
    }

    public EntityResponse<Storage> getStorageResponse(Long id) {
        Storage storage = getStorage(id);
        return EntityResponse.success( "Found your storage! It was hiding behind the boxes ",storage);
    }

    /**
     * Creates a new storage location.
     */
    @Transactional
    public EntityResponse<Storage> addStorageResponse(StorageDTO storageDTO) {
        try {
            Storage storage = new Storage();
            mapDTOToStorage(storageDTO, storage);
            return createStorage(storage);
        } catch (Exception e) {
            logger.error("Error creating new storage: {}", storageDTO, e);
            throw new RuntimeException("Failed to create new storage location. Please check your input and try again.");
        }
    }

    public EntityResponse<Storage> createStorage(Storage storage) {
        // Validate storage data
        if (storage.getName() == null || storage.getName().trim().isEmpty()) {
            throw new GlobalExceptionHandler.InvalidStorageOperationException("A storage needs a name! We can't just call it 'that place with stuff'");
        }

        // Check if storage already exists at this location
        Location location = storage.getLocation();
        if (location == null) {
            throw new GlobalExceptionHandler.InvalidStorageOperationException("A storage needs a location! It can't float in space!");
        }

        List<Storage> existingStorages = storageRepository.findAll();
        boolean locationExists = existingStorages.stream()
            .anyMatch(s -> isLocationEqual(s.getLocation(), location));

        if (locationExists) {
            throw new GlobalExceptionHandler.StorageAlreadyExistsException(location.toString());
        }

        Storage savedStorage = storageRepository.save(storage);
        return EntityResponse.success("Welcome to the family! Your storage has been successfully created 🏪", savedStorage);
    }

    /**
     * Helper method to compare two locations for equality
     * Because sometimes two places can be the same, just like two doors can lead to the same room! 🚪
     */
    private boolean isLocationEqual(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return false;
        return loc1.getLatitude().equals(loc2.getLatitude()) &&
               loc1.getLongitude().equals(loc2.getLongitude());
    }

    /**
     * Updates an existing storage location.
     */
    @Transactional
    public EntityResponse<Storage> updateStorageResponse(Long id, StorageDTO storageDTO) {
        try {
            Storage existingStorage = getStorage(id);
            Storage updatedStorage = new Storage();
            mapDTOToStorage(storageDTO, updatedStorage);
            return updateStorage(id, updatedStorage);
        } catch (Exception e) {
            logger.error("Error updating storage with id: {}", id, e);
            throw new RuntimeException("Failed to  update storage location. Please check your input and try again.");
        }
    }

    public EntityResponse<Storage> updateStorage(Long id, Storage updatedStorage) {
        Storage existingStorage = getStorage(id);
        
        // Check if new location conflicts with existing storage
        Location newLocation = updatedStorage.getLocation();
        if (newLocation != null && 
            !isLocationEqual(existingStorage.getLocation(), newLocation)) {
            
            List<Storage> existingStorages = storageRepository.findAll();
            boolean locationExists = existingStorages.stream()
                .filter(s -> !s.getId().equals(id)) // Exclude current storage
                .anyMatch(s -> isLocationEqual(s.getLocation(), newLocation));

            if (locationExists) {
                throw new GlobalExceptionHandler.StorageAlreadyExistsException(newLocation.toString());
            }
        }

        // Update fields
        if (updatedStorage.getName() != null) {
            existingStorage.setName(updatedStorage.getName());
        }
        if (newLocation != null) {
            existingStorage.setLocation(newLocation);
        }
        if (updatedStorage.getProductTypes() != null) {
            existingStorage.setProductTypes(updatedStorage.getProductTypes());
        }

        Storage savedStorage = storageRepository.save(existingStorage);
        return EntityResponse.success("Storage makeover complete! Looking better than ever ✨", savedStorage);
    }

    /**
     * Deletes a storage location.
     */
    @Transactional
    public EntityResponse<Void> deleteStorageResponse(Long id) {
        try {
            return deleteStorage(id);
        } catch (Exception e) {
            logger.error("Error deleting storage with id: {}", id, e);
            throw new RuntimeException("Failed to delete storage location. It might be referenced by other entities.");
        }
    }

    public EntityResponse<Void> deleteStorage(Long id) {
        Storage storage = getStorage(id);
        storageRepository.delete(storage);
        return EntityResponse.success("Storage has checked out! Thanks for staying with us ");
    }

    /**
     * Finds the nearest storage location to given coordinates.
     */
    public EntityResponse<Storage> findNearestStorageResponse(Double latitude, Double longitude) {
        try {
            List<Storage> storages = storageRepository.findAll();
            if (storages.isEmpty()) {
                throw new ResourceNotFoundException("No storage locations found in the system! ");
            }

            Storage nearestStorage = storages.stream()
                .min((s1, s2) -> {
                    double dist1 = calculateDistance(latitude, longitude, 
                        s1.getLocation().getLatitude(), s1.getLocation().getLongitude());
                    double dist2 = calculateDistance(latitude, longitude, 
                        s2.getLocation().getLatitude(), s2.getLocation().getLongitude());
                    return Double.compare(dist1, dist2);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Error finding nearest storage"));

            double distance = calculateDistance(latitude, longitude,
                nearestStorage.getLocation().getLatitude(),
                nearestStorage.getLocation().getLongitude());

            return new EntityResponse<>(
                String.format("Found nearest storage '%s' (%.2f km away) ", 
                    nearestStorage.getName(), distance),
                true,
                nearestStorage
            );
        } catch (ResourceNotFoundException e) {
            logger.warn("No storage locations found for coordinates: {}, {}", latitude, longitude);
            throw e;
        } catch (Exception e) {
            logger.error("Error finding nearest storage for coordinates: {}, {}", latitude, longitude, e);
            throw new RuntimeException("Failed to find nearest storage location. Please try again later.");
        }
    }

    /**
     * Maps StorageDTO to Storage entity.
     */
    private void mapDTOToStorage(StorageDTO dto, Storage storage) {
        storage.setName(dto.getName());
        
        Location location = storage.getLocation();
        if (location == null) {
            location = new Location();
            storage.setLocation(location);
        }
        
        location.setLatitude(dto.getLocation().getLatitude());
        location.setLongitude(dto.getLocation().getLongitude());
        location.setMarkerTitle(dto.getLocation().getMarkerTitle());
        
        storage.setProductTypes(dto.getProductTypes());
    }

    /**
     * Calculates distance between two points using Haversine formula.
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's radius in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
}
