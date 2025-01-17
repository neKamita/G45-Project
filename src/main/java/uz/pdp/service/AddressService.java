package uz.pdp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import uz.pdp.dto.AddressDTO;
import uz.pdp.entity.Address;
import uz.pdp.entity.Location;
import uz.pdp.entity.User;
import uz.pdp.payload.EntityResponse;
import uz.pdp.repository.AddressRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service class for managing address-related operations.
 * Handles creation, retrieval, update, and deletion of addresses.
 * Includes functionality for managing default addresses and finding nearest locations.
 *
 * @version 1.0
 * @since 2025-01-17
 */
@Service
public class AddressService {
    
    private static final Logger logger = LoggerFactory.getLogger(AddressService.class);
    
    private final AddressRepository addressRepository;
    private final UserService userService;

    @Autowired
    public AddressService(AddressRepository addressRepository, UserService userService) {
        this.addressRepository = addressRepository;
        this.userService = userService;
    }

    /**
     * Creates a new address for the current user.
     * Validates address details and sets default if it's the user's first address.
     *
     * @param addressDTO DTO containing address details
     * @return EntityResponse with created address
     * @throws IllegalArgumentException if required fields are missing
     */
    @Transactional
    public EntityResponse<Address> createAddress(AddressDTO addressDTO) {
        try {
            User currentUser = userService.getCurrentUser();
            validateAddressDTO(addressDTO);

            Address address = new Address();
            mapDTOToAddress(addressDTO, address);
            address.setUser(currentUser);

            // Set as default if it's the user's first address
            if (addressRepository.countByUserId(currentUser.getId()) == 0) {
                address.setDefault(true);
            }

            Address savedAddress = addressRepository.save(address);
            logger.info("Created new address for user ID: {}", currentUser.getId());
            return new EntityResponse<>("Address created successfully", true, savedAddress);
        } catch (Exception e) {
            logger.error("Error creating address: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves all addresses for the current user.
     *
     * @return EntityResponse with list of addresses
     */
    public EntityResponse<List<Address>> getUserAddresses() {
        try {
            User currentUser = userService.getCurrentUser();
            List<Address> addresses = addressRepository.findByUserId(currentUser.getId());
            logger.info("Retrieved {} addresses for user ID: {}", addresses.size(), currentUser.getId());
            return new EntityResponse<>("Addresses retrieved successfully", true, addresses);
        } catch (Exception e) {
            logger.error("Error retrieving addresses: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Updates an existing address.
     * Verifies that the address belongs to the current user.
     *
     * @param id Address ID to update
     * @param addressDTO Updated address details
     * @return EntityResponse with updated address
     * @throws IllegalArgumentException if address not found or not owned by user
     */
    @Transactional
    public EntityResponse<Address> updateAddress(Long id, AddressDTO addressDTO) {
        try {
            User currentUser = userService.getCurrentUser();
            Address address = addressRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Address not found or not owned by user"));

            validateAddressDTO(addressDTO);
            mapDTOToAddress(addressDTO, address);

            Address updatedAddress = addressRepository.save(address);
            logger.info("Updated address ID: {} for user ID: {}", id, currentUser.getId());
            return new EntityResponse<>("Address updated successfully", true, updatedAddress);
        } catch (Exception e) {
            logger.error("Error updating address {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Deletes an address.
     * Verifies that the address belongs to the current user.
     * If the deleted address was default, sets another address as default if available.
     *
     * @param id Address ID to delete
     * @return EntityResponse with deletion status
     * @throws IllegalArgumentException if address not found or not owned by user
     */
    @Transactional
    public EntityResponse<Void> deleteAddress(Long id) {
        try {
            User currentUser = userService.getCurrentUser();
            Address address = addressRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Address not found or not owned by user"));

            boolean wasDefault = address.isDefault();
            addressRepository.delete(address);

            // If deleted address was default, set another address as default
            if (wasDefault) {
                Optional<Address> firstAddress = addressRepository.findFirstByUserId(currentUser.getId());
                firstAddress.ifPresent(addr -> {
                    addr.setDefault(true);
                    addressRepository.save(addr);
                });
            }

            logger.info("Deleted address ID: {} for user ID: {}", id, currentUser.getId());
            return new EntityResponse<>("Address deleted successfully", true, null);
        } catch (Exception e) {
            logger.error("Error deleting address {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Sets an address as the default shipping address.
     * Removes default status from any other address of the user.
     *
     * @param id Address ID to set as default
     * @return EntityResponse with updated address
     * @throws IllegalArgumentException if address not found or not owned by user
     */
    @Transactional
    public EntityResponse<Address> setDefaultAddress(Long id) {
        try {
        
            User currentUser = userService.getCurrentUser();
            Address address = addressRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Address not found or not owned by user"));

            // Remove default status from other addresses
            addressRepository.removeDefaultStatus(currentUser.getId());

            // Set new default address
            address.setDefault(true);
            Address updatedAddress = addressRepository.save(address);

            logger.info("Set address ID: {} as default for user ID: {}", id, currentUser.getId());
            return new EntityResponse<>("Address set as default successfully", true, updatedAddress);
        } catch (Exception e) {
            logger.error("Error setting default address {}: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * Validates address DTO fields.
     *
     * @param addressDTO DTO to validate
     * @throws IllegalArgumentException if required fields are missing or invalid
     */
    private void validateAddressDTO(AddressDTO addressDTO) {
        if (addressDTO.getStreet() == null || addressDTO.getStreet().trim().isEmpty()) {
            throw new IllegalArgumentException("Street is required");
        }
        if (addressDTO.getCity() == null || addressDTO.getCity().trim().isEmpty()) {
            throw new IllegalArgumentException("City is required");
        }
        if (addressDTO.getCountry() == null || addressDTO.getCountry().trim().isEmpty()) {
            throw new IllegalArgumentException("Country is required");
        }
    }

    /**
     * Maps DTO fields to Address entity.
     *
     * @param dto DTO containing address details
     * @param address Address entity to update
     */
    private void mapDTOToAddress(AddressDTO dto, Address address) {
        address.setName(dto.getName());
        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setPhone(dto.getPhone());
        address.setWorkingHours(dto.getWorkingHours());
        address.setEmail(dto.getEmail());
        address.setSocialLinks(dto.getSocialLinks());

        Location location = address.getLocation();
        if (location == null) {
            location = new Location();
            location.setAddress(address);
        }
        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());
        location.setMarkerTitle(dto.getName());
        
        address.setLocation(location);
    }

    public List<Address> getAllAddresses() {
        logger.info("Fetching all addresses ordered by city");
        return addressRepository.findAllByOrderByCity();
    }

    public Address getAddress(Long id) {
        logger.info("Fetching address with id: {}", id);
        return addressRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + id));
    }

    public List<Location> getAllMapPoints() {
        logger.info("Fetching all map points");
        return getAllAddresses().stream()
            .map(Address::getLocation)
            .collect(Collectors.toList());
    }

    public Address addAddress(AddressDTO dto) {
        logger.info("Adding new address: {}", dto);
        Address address = new Address();
        mapDTOToAddress(dto, address);
        return addressRepository.save(address);
    }

    public Address updateAddress(Long id, AddressDTO dto) {
        logger.info("Updating address with id: {}", id);
        Address address = getAddress(id);
        mapDTOToAddress(dto, address);
        return addressRepository.save(address);
    }

    public void deleteAddress(Long id) {
        logger.info("Deleting address with id: {}", id);
        addressRepository.deleteById(id);
    }

    public List<Address> searchAddressesByCity(String city) {
        logger.info("Searching addresses in city: {}", city);
        return addressRepository.findByCityContainingIgnoreCase(city);
    }

    public Address findNearestAddress(Double latitude, Double longitude) {
        logger.info("Finding nearest address to coordinates: {}, {}", latitude, longitude);
        return getAllAddresses().stream()
            .min((a1, a2) -> compareDistances(a1, a2, latitude, longitude))
            .orElseThrow(() -> new EntityNotFoundException("No addresses found"));
    }

    public ResponseEntity<EntityResponse<Address>> findNearestAddressResponse(Double latitude, Double longitude) {
        try {
            Address address = findNearestAddress(latitude, longitude);
            return ResponseEntity.ok(EntityResponse.success(
                "Nearest address found successfully", address));
        } catch (Exception e) {
            logger.error("Error finding nearest address: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(EntityResponse.error("Failed to find nearest address: " + e.getMessage()));
        }
    }

    private int compareDistances(Address a1, Address a2, Double lat, Double lon) {
        double d1 = calculateDistance(a1.getLocation().getLatitude(), a1.getLocation().getLongitude(), lat, lon);
        double d2 = calculateDistance(a2.getLocation().getLatitude(), a2.getLocation().getLongitude(), lat, lon);
        return Double.compare(d1, d2);
    }

    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        // Haversine formula for calculating distance between coordinates
        double R = 6371; // Earth's radius in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    public ResponseEntity<EntityResponse<List<Address>>> getAllAddressesResponse() {
        try {
            List<Address> addresses = addressRepository.findAll();
            return ResponseEntity.ok(new EntityResponse<>("Addresses retrieved successfully", true, addresses));
        } catch (Exception e) {
            logger.error("Error retrieving all addresses: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new EntityResponse<>("Error retrieving addresses", false, null));
        }
    }

    public ResponseEntity<EntityResponse<Address>> getAddressResponse(Long id) {
        try {
            Address address = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + id));
            return ResponseEntity.ok(new EntityResponse<>("Address retrieved successfully", true, address));
        } catch (EntityNotFoundException e) {
            logger.error("Address not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new EntityResponse<>(e.getMessage(), false, null));
        } catch (Exception e) {
            logger.error("Error retrieving address: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new EntityResponse<>("Error retrieving address", false, null));
        }
    }

    public ResponseEntity<EntityResponse<List<Location>>> getAllMapPointsResponse() {
        try {
            List<Location> points = getAllAddresses().stream()
                .map(Address::getLocation)
                .collect(Collectors.toList());
            return ResponseEntity.ok(new EntityResponse<>("Map points retrieved successfully", true, points));
        } catch (Exception e) {
            logger.error("Error retrieving map points: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new EntityResponse<>("Failed to retrieve map points: " + e.getMessage(), false, null));
        }
    }

    public ResponseEntity<EntityResponse<Address>> addAddressResponse(AddressDTO dto) {
        try {
            Address address = new Address();
            mapDTOToAddress(dto, address);
            address = addressRepository.save(address);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new EntityResponse<>("Address added successfully", true, address));
        } catch (Exception e) {
            logger.error("Error adding address: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new EntityResponse<>("Failed to add address: " + e.getMessage(), false, null));
        }
    }

    public ResponseEntity<EntityResponse<Address>> updateAddressResponse(Long id, AddressDTO dto) {
        try {
            Address address = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + id));
            mapDTOToAddress(dto, address);
            address = addressRepository.save(address);
            return ResponseEntity.ok(new EntityResponse<>("Address updated successfully", true, address));
        } catch (EntityNotFoundException e) {
            logger.error("Address not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new EntityResponse<>(e.getMessage(), false, null));
        } catch (Exception e) {
            logger.error("Error updating address: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new EntityResponse<>("Failed to update address: " + e.getMessage(), false, null));
        }
    }

    public ResponseEntity<EntityResponse<Void>> deleteAddressResponse(Long id) {
        try {
            if (!addressRepository.existsById(id)) {
                throw new EntityNotFoundException("Address not found with id: " + id);
            }
            addressRepository.deleteById(id);
            return ResponseEntity.ok(new EntityResponse<>("Address deleted successfully", true, null));
        } catch (EntityNotFoundException e) {
            logger.error("Address not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new EntityResponse<>(e.getMessage(), false, null));
        } catch (Exception e) {
            logger.error("Error deleting address: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new EntityResponse<>("Failed to delete address: " + e.getMessage(), false, null));
        }
    }

    public ResponseEntity<EntityResponse<List<Address>>> searchAddressesByCityResponse(String city) {
        try {
            List<Address> addresses = addressRepository.findByCityContainingIgnoreCase(city);
            String message = addresses.isEmpty() 
                ? String.format("No addresses found in city: %s", city)
                : String.format("Found %d addresses in %s", addresses.size(), city);
            return ResponseEntity.ok(new EntityResponse<>(message, true, addresses));
        } catch (Exception e) {
            logger.error("Error searching addresses: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new EntityResponse<>("Failed to search addresses: " + e.getMessage(), false, null));
        }
    }
}
