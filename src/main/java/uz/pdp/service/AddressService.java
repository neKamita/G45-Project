package uz.pdp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.pdp.dto.AddressDTO;
import uz.pdp.entity.Address;
import uz.pdp.entity.Location;
import uz.pdp.entity.User;
import uz.pdp.enums.Role;
import uz.pdp.exception.BadRequestException;
import uz.pdp.exception.ResourceNotFoundException;
import uz.pdp.payload.EntityResponse;
import uz.pdp.repository.AddressRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * Service class for managing address-related operations.
 * Handles creation, retrieval, update, and deletion of addresses.
 * Includes functionality for managing default addresses and finding nearest
 * locations.
 *
 * @version 1.0
 * @since 2025-01-17
 */
@Service
public class AddressService {

    private static final Logger logger = LoggerFactory.getLogger(AddressService.class);

    private final AddressRepository addressRepository;
    private final UserService userService;

    public AddressService(AddressRepository addressRepository, UserService userService) {
        this.addressRepository = addressRepository;
        this.userService = userService;
    }

    /**
     * Creates a new address for the current user.
     * Only SELLER and ADMIN roles can create addresses.
     * Validates address details and sets default if it's the user's first address.
     *
     * @param addressDTO DTO containing address details
     * @return EntityResponse with created address
     * @throws BadRequestException if required fields are missing or user doesn't have permission
     */
    @Transactional
    public ResponseEntity<EntityResponse<Address>> addAddressResponse(AddressDTO addressDTO) {
        try {
            logger.info("Adding new address: {}", addressDTO);

            User currentUser = userService.getCurrentUser();

            // Check if user has permission to create addresses
            if (currentUser.getRole() != Role.SELLER && currentUser.getRole() != Role.ADMIN) {
                throw new BadRequestException("Only sellers and administrators can create store addresses");
            }

            // Check if this is the first address (it will be default)
            long addressCount = addressRepository.countByUserId(currentUser.getId());
            if (addressCount == 0) {
                addressDTO.setDefault(true);
            }

            // If this address is set as default, unset any existing default address
            if (addressDTO.isDefault()) {
                Optional<Address> existingDefault = addressRepository.findByUserIdAndIsDefaultTrue(currentUser.getId());
                existingDefault.ifPresent(address -> {
                    address.setDefault(false);
                    addressRepository.save(address);
                });
            }

            // Create new address
            Address address = new Address();
            address.setUser(currentUser);
            address.setName(addressDTO.getName());
            address.setStreet(addressDTO.getStreet());
            address.setCity(addressDTO.getCity());
            address.setPhoneNumber(addressDTO.getPhoneNumber());
            address.setWorkingHours(addressDTO.getWorkingHours());
            address.setEmail(addressDTO.getEmail());
            address.setDefault(addressDTO.isDefault());
            address.setSocialLinks(addressDTO.getSocialLinks());

            // Create and set location
            if (addressDTO.getLocation() != null) {
                Location location = new Location();
                location.setLatitude(addressDTO.getLocation().getLatitude());
                location.setLongitude(addressDTO.getLocation().getLongitude());
                location.setMarkerTitle(addressDTO.getLocation().getMarkerTitle());
                location.setAddress(address);
                address.setLocation(location);
            }

            address = addressRepository.save(address);
            logger.info("Successfully added new address with ID: {}", address.getId());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(EntityResponse.success("Store address added successfully", address));

        } catch (BadRequestException e) {
            logger.error("Permission error adding new address: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error adding new address: {}", e.getMessage());
            throw new BadRequestException("Failed to add store address: " + e.getMessage());
        }
    }

    /**
     * Retrieves all addresses for the current user.
     *
     * @return EntityResponse with list of addresses
     */
    @Transactional
    public ResponseEntity<EntityResponse<List<Address>>> getAllAddressesResponse() {
        try {
            logger.info("Fetching all store addresses");
            List<Address> addresses = addressRepository.findByUserId(userService.getCurrentUser().getId());
            return ResponseEntity.ok(EntityResponse.success("Addresses retrieved successfully", addresses));
        } catch (Exception e) {
            logger.error("Error retrieving all addresses: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EntityResponse.error("Error retrieving addresses: " + e.getMessage()));
        }
    }

    /**
     * Retrieves an address by ID.
     *
     * @param id Address ID to retrieve
     * @return EntityResponse with retrieved address
     * @throws ResourceNotFoundException if address not found
     */
    @Transactional
    public ResponseEntity<EntityResponse<Address>> getAddressResponse(Long id) {
        try {
            logger.info("Fetching address with id: {}", id);
            Address address = addressRepository.findByIdAndUserId(id, userService.getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
            return ResponseEntity.ok(EntityResponse.success("Address retrieved successfully", address));
        } catch (ResourceNotFoundException e) {
            logger.error("Address not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(EntityResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error retrieving address: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EntityResponse.error("Error retrieving address: " + e.getMessage()));
        }
    }

    /**
     * Updates an existing address.
     * Verifies that the address belongs to the current user.
     *
     * @param id         Address ID to update
     * @param addressDTO Updated address details
     * @return EntityResponse containing the updated address DTO
     * @throws BadRequestException if address not found or not owned by user
     */
    @Transactional
    public EntityResponse<?> updateAddress(Long id, AddressDTO addressDTO) {
        try {
            validateAddressDTO(addressDTO);
            Address address = addressRepository.findByIdAndUserId(id, userService.getCurrentUser().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found or not owned by current user"));

            mapDTOToAddress(addressDTO, address);
            if (addressDTO.isDefault()) {
                setDefaultAddress(address);
            }

            Address updatedAddress = addressRepository.save(address);
            return EntityResponse.success("Address updated successfully", updatedAddress);
        } catch (BadRequestException | ResourceNotFoundException e) {
            logger.error("Error updating address: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error updating address", e);
            throw new BadRequestException("Failed to update address");
        }
    }

    /**
     * Deletes an address.
     * Only admins can delete any address, users can only delete their own.
     *
     * @param id ID of the address to delete
     * @return EntityResponse indicating success/failure
     * @throws ResourceNotFoundException if address not found
     * @throws BadRequestException if user doesn't have permission
     */
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public EntityResponse<Void> deleteAddress(Long id) {
        try {
            logger.info("Deleting address with ID: {}", id);
            User currentUser = userService.getCurrentUser();
            Address address;

            if (currentUser.getRole() == Role.ADMIN) {
                address = addressRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
            } else {
                address = addressRepository.findByIdAndUserId(id, currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found or access denied"));
            }

            addressRepository.delete(address);
            logger.info("Successfully deleted address with ID: {}", id);
            return EntityResponse.success("Address deleted successfully");
        } catch (ResourceNotFoundException e) {
            logger.error("Address not found - ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting address {}: {}", id, e.getMessage());
            throw new BadRequestException("Failed to delete address: " + e.getMessage());
        }
    }

    /**
     * Searches addresses by city.
     *
     * @param city City to search for
     * @return EntityResponse with list of addresses
     * @throws BadRequestException if city parameter is empty
     */
    @Transactional
    public ResponseEntity<EntityResponse<List<Address>>> searchAddressesByCityResponse(String city) {
        try {
            logger.info("Searching addresses by city: {}", city);
            if (city == null || city.trim().isEmpty()) {
                throw new BadRequestException("City parameter cannot be empty");
            }
            List<Address> addresses = addressRepository.findByCityContainingIgnoreCaseAndUserId(city.trim(), userService.getCurrentUser().getId());
            return ResponseEntity.ok(EntityResponse.success("Addresses found successfully", addresses));
        } catch (BadRequestException e) {
            logger.error("Invalid city parameter: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error searching addresses: {}", e.getMessage());
            throw new BadRequestException("Failed to search addresses: " + e.getMessage());
        }
    }

    /**
     * Retrieves all map points.
     *
     * @return EntityResponse with list of map points
     */
    @Transactional
    public ResponseEntity<EntityResponse<List<AddressDTO.LocationDTO>>> getAllMapPointsResponse() {
        try {
            logger.info("Fetching all map points");
            List<AddressDTO.LocationDTO> points = addressRepository.findAllByUserId(userService.getCurrentUser().getId()).stream()
                    .map(address -> {
                        Address addr = (Address) address;
                        Location location = addr.getLocation();
                        if (location == null) {
                            return null;
                        }
                        AddressDTO.LocationDTO locationDTO = new AddressDTO.LocationDTO();
                        locationDTO.setLatitude(location.getLatitude());
                        locationDTO.setLongitude(location.getLongitude());
                        locationDTO.setMarkerTitle(location.getMarkerTitle());
                        return locationDTO;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(EntityResponse.success("Map points retrieved successfully", points));
        } catch (Exception e) {
            logger.error("Error retrieving map points: {}", e.getMessage());
            throw new BadRequestException("Failed to retrieve map points: " + e.getMessage());
        }
    }

    /**
     * Find the nearest address to given coordinates
     * 
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @return EntityResponse containing the nearest address
     */
    public EntityResponse<?> findNearestAddress(Double latitude, Double longitude) {
        List<Address> nearestAddresses = addressRepository.findNearestAddresses(latitude, longitude);
        if (nearestAddresses.isEmpty()) {
            throw new ResourceNotFoundException("No addresses found");
        }
        
        Address nearestAddress = nearestAddresses.get(0);
        return EntityResponse.success("Found nearest address", nearestAddress);
    }

    /**
     * Sets an address as default and removes default status from other addresses
     * 
     * @param newDefaultAddress Address to set as default
     */
    private void setDefaultAddress(Address newDefaultAddress) {
        if (newDefaultAddress == null) {
            throw new BadRequestException("Address cannot be null");
        }

        // Get current user's default address
        Optional<Address> currentDefault = addressRepository.findByUserIdAndIsDefaultTrue(newDefaultAddress.getUser().getId());
        
        // If there's a current default address and it's different from the new one
        if (currentDefault.isPresent() && !currentDefault.get().getId().equals(newDefaultAddress.getId())) {
            Address oldDefault = currentDefault.get();
            oldDefault.setDefault(false);
            addressRepository.save(oldDefault);
        }

        // Set the new address as default
        newDefaultAddress.setDefault(true);
    }

    private void validateAddressDTO(AddressDTO addressDTO) {
        if (addressDTO == null) {
            throw new BadRequestException("Address data cannot be null");
        }
        if (addressDTO.getName() == null || addressDTO.getName().trim().isEmpty()) {
            throw new BadRequestException("Address name cannot be empty");
        }
        if (addressDTO.getStreet() == null || addressDTO.getStreet().trim().isEmpty()) {
            throw new BadRequestException("Street is required");
        }
        if (addressDTO.getCity() == null || addressDTO.getCity().trim().isEmpty()) {
            throw new BadRequestException("City is required");
        }
        if (addressDTO.getPhoneNumber() != null && !addressDTO.getPhoneNumber().trim().isEmpty()) {
            // Validate phone number format
            String phoneNumber = addressDTO.getPhoneNumber().trim();
            if (!phoneNumber.matches("^\\+?[0-9]{10,15}$")) {
                throw new BadRequestException("Invalid phone number format. Must be 10-15 digits with optional + prefix");
            }
            // Check for duplicate phone numbers
            if (addressRepository.existsByPhoneNumberIgnoreCase(phoneNumber)) {
                throw new BadRequestException("Address with this phone number already exists");
            }
        }
    }

    private void mapDTOToAddress(AddressDTO dto, Address address) {
        address.setName(dto.getName());
        address.setStreet(dto.getStreet());
        address.setCity(dto.getCity());
        address.setPhoneNumber(dto.getPhoneNumber());
        address.setWorkingHours(dto.getWorkingHours());
        address.setEmail(dto.getEmail());
        address.setDefault(false); // Initialize with false by default

        Location location = address.getLocation();
        if (location == null) {
            location = new Location();
            location.setAddress(address);
        }
        location.setLatitude(dto.getLocation().getLatitude());
        location.setLongitude(dto.getLocation().getLongitude());
        location.setMarkerTitle(dto.getName());

        address.setLocation(location);
    }

    /**
     * Retrieves all addresses in the system.
     * Only accessible by ADMIN users.
     *
     * @return List of all addresses
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<Address> getAllAddresses() {
        try {
            logger.info("Retrieving all addresses");
            List<Address> addresses = addressRepository.findAll();
            logger.info("Retrieved {} addresses", addresses.size());
            return addresses;
        } catch (Exception e) {
            logger.error("Error retrieving all addresses: {}", e.getMessage());
            throw new BadRequestException("Failed to retrieve addresses: " + e.getMessage());
        }
    }

    /**
     * Retrieves an address by its ID.
     * Users can only access their own addresses unless they are ADMIN.
     *
     * @param id ID of the address to retrieve
     * @return Address if found
     * @throws ResourceNotFoundException if address not found
     */
    public Address getAddressById(Long id) {
        try {
            logger.info("Retrieving address with ID: {}", id);
            User currentUser = userService.getCurrentUser();
            Address address;
            
            if (currentUser.getRole() == Role.ADMIN) {
                address = addressRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
            } else {
                address = addressRepository.findByIdAndUserId(id, currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Address not found or access denied"));
            }
            
            logger.info("Retrieved address: {}", address.getStreet());
            return address;
        } catch (ResourceNotFoundException e) {
            logger.error("Address not found - ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving address {}: {}", id, e.getMessage());
            throw new BadRequestException("Failed to retrieve address: " + e.getMessage());
        }
    }

    /**
     * Retrieves all addresses in a specific city.
     * Only accessible by ADMIN users.
     *
     * @param city Name of the city
     * @return List of addresses in the city
     */
    @PreAuthorize("hasRole('ADMIN')")
    public List<Address> getAddressesByCity(String city) {
        try {
            logger.info("Retrieving addresses for city: {}", city);
            List<Address> addresses = addressRepository.findByCity(city);
            logger.info("Retrieved {} addresses for city {}", addresses.size(), city);
            return addresses;
        } catch (Exception e) {
            logger.error("Error retrieving addresses for city {}: {}", city, e.getMessage());
            throw new BadRequestException("Failed to retrieve addresses: " + e.getMessage());
        }
    }

    /**
     * Creates a new address.
     * Converts Address entity to AddressDTO for creation.
     *
     * @param address Address entity to create
     * @return EntityResponse containing created address
     */
    public EntityResponse<Address> createAddress(Address address) {
        try {
            logger.info("Creating new address");
            AddressDTO addressDTO = convertToDTO(address);
            Address createdAddress = addressRepository.save(address);
            logger.info("Created address with ID: {}", createdAddress.getId());
            return EntityResponse.success("Address created successfully", createdAddress);
        } catch (Exception e) {
            logger.error("Error creating address: {}", e.getMessage());
            throw new BadRequestException("Failed to create address: " + e.getMessage());
        }
    }

    /**
     * Updates an existing address.
     * Converts Address entity to AddressDTO for update.
     *
     * @param id ID of the address to update
     * @param updatedAddress Updated address details
     * @return EntityResponse containing updated address
     */
    public EntityResponse<Address> updateAddress(Long id, Address updatedAddress) {
        try {
            logger.info("Updating address with ID: {}", id);
            Address existingAddress = getAddressById(id);
            
            // Update fields
            existingAddress.setName(updatedAddress.getName());
            existingAddress.setStreet(updatedAddress.getStreet());
            existingAddress.setCity(updatedAddress.getCity());
            existingAddress.setPhoneNumber(updatedAddress.getPhoneNumber());
            existingAddress.setWorkingHours(updatedAddress.getWorkingHours());
            existingAddress.setEmail(updatedAddress.getEmail());
            
            if (updatedAddress.getLocation() != null) {
                if (existingAddress.getLocation() == null) {
                    existingAddress.setLocation(new Location());
                }
                existingAddress.getLocation().setLatitude(updatedAddress.getLocation().getLatitude());
                existingAddress.getLocation().setLongitude(updatedAddress.getLocation().getLongitude());
            }
            
            Address savedAddress = addressRepository.save(existingAddress);
            logger.info("Updated address with ID: {}", savedAddress.getId());
            return EntityResponse.success("Address updated successfully", savedAddress);
        } catch (Exception e) {
            logger.error("Error updating address {}: {}", id, e.getMessage());
            throw new BadRequestException("Failed to update address: " + e.getMessage());
        }
    }

    /**
     * Validates an address.
     * Checks if the address fields are properly formatted and valid.
     *
     * @param address Address to validate
     * @return true if address is valid
     */
    public boolean validateAddress(Address address) {
        try {
            logger.info("Validating address");
            
            // Basic validation
            if (address == null) {
                return false;
            }
            
            if (address.getName() == null || address.getName().trim().isEmpty() ||
                address.getStreet() == null || address.getStreet().trim().isEmpty() ||
                address.getCity() == null || address.getCity().trim().isEmpty() ||
                address.getPhoneNumber() == null || address.getPhoneNumber().trim().isEmpty()) {
                return false;
            }
            
            // Location validation
            if (address.getLocation() != null) {
                Location location = address.getLocation();
                if (location.getLatitude() == null || location.getLongitude() == null) {
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            logger.warn("Address validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Converts Address entity to AddressDTO.
     *
     * @param address Address entity to convert
     * @return AddressDTO
     */
    private AddressDTO convertToDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setName(address.getName());
        dto.setStreet(address.getStreet());
        dto.setCity(address.getCity());
        dto.setPhoneNumber(address.getPhoneNumber());
        dto.setWorkingHours(address.getWorkingHours());
        dto.setEmail(address.getEmail());
        dto.setDefault(address.isDefault());
        
        if (address.getLocation() != null) {
            AddressDTO.LocationDTO locationDTO = new AddressDTO.LocationDTO();
            locationDTO.setLatitude(address.getLocation().getLatitude());
            locationDTO.setLongitude(address.getLocation().getLongitude());
            dto.setLocation(locationDTO);
        }
        
        if (address.getSocialLinks() != null) {
            dto.setSocialLinks(address.getSocialLinks());
        }
        
        return dto;
    }

    /**
     * Updates an address and returns a ResponseEntity.
     * Wraps updateAddress method for controller use.
     *
     * @param id Address ID to update
     * @param addressDTO Updated address details
     * @return ResponseEntity with updated address
     */
    @Transactional
    public ResponseEntity<EntityResponse<Address>> updateAddressResponse(Long id, AddressDTO addressDTO) {
        try {
            EntityResponse<Address> response = (EntityResponse<Address>) updateAddress(id, addressDTO);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(EntityResponse.error(e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(EntityResponse.error(e.getMessage()));
        }
    }

    /**
     * Deletes an address and returns a ResponseEntity.
     * Wraps deleteAddress method for controller use.
     *
     * @param id Address ID to delete
     * @return ResponseEntity with deletion result
     */
    @Transactional
    public ResponseEntity<EntityResponse<Void>> deleteAddressResponse(Long id) {
        try {
            EntityResponse<Void> response = deleteAddress(id);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(EntityResponse.error(e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(EntityResponse.error(e.getMessage()));
        }
    }

    /**
     * Finds nearest address and returns a ResponseEntity.
     * Wraps findNearestAddress method for controller use.
     *
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @return ResponseEntity with nearest address
     */
    @Transactional
    public ResponseEntity<EntityResponse<Address>> findNearestAddressResponse(Double latitude, Double longitude) {
        try {
            EntityResponse<Address> response = (EntityResponse<Address>) findNearestAddress(latitude, longitude);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(EntityResponse.error(e.getMessage()));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(EntityResponse.error(e.getMessage()));
        }
    }
}
