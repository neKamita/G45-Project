package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uz.pdp.dto.AddressDTO;
import uz.pdp.entity.Address;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.AddressService;
import uz.pdp.entity.Location;

import java.util.List;

/**
 * REST controller for managing user contact information and addresses.
 * Provides endpoints for creating, updating, retrieving, and deleting address information.
 * Most operations require authentication and proper authorization.
 *
 * @version 1.0
 * @since 2025-01-17
 */
@RestController
@RequestMapping("/api/contacts")
@Tag(name = "Contact Management", description = "APIs for managing user contact information and addresses")
public class ContactController {

    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);

    private final AddressService addressService;

    @Autowired
    public ContactController(AddressService addressService) {
        this.addressService = addressService;
    }

    /**
     * Retrieves all store addresses.
     *
     * @return ResponseEntity with EntityResponse containing a list of addresses.
     *         - 200 OK with list of addresses
     *         - 404 Not Found if no addresses exist
     */
    @GetMapping("/addresses")
    @Operation(summary = "Get all store addresses")
    public ResponseEntity<EntityResponse<List<Address>>> getAllAddresses() {
        try {
            logger.info("Fetching all store addresses");
            ResponseEntity<EntityResponse<List<Address>>> response = addressService.getAllAddressesResponse();
            logger.info("Successfully retrieved {} addresses",
                    response.getBody() != null && response.getBody().getData() != null ?
                            response.getBody().getData().size() : 0);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error fetching all addresses: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EntityResponse<>("Failed to fetch addresses: " + e.getMessage(), false, null));
        }
    }

    /**
     * Retrieves address details by ID.
     *
     * @param id Address ID.
     * @return ResponseEntity with EntityResponse containing address details.
     *         - 200 OK with address details
     *         - 404 Not Found if address doesn't exist
     */
    @GetMapping("/addresses/{id}")
    @Operation(summary = "Get address details by ID")
    public ResponseEntity<EntityResponse<Address>> getAddress(@PathVariable Long id) {
        try {
            logger.info("Fetching address with id: {}", id);
            ResponseEntity<EntityResponse<Address>> response = addressService.getAddressResponse(id);
            if (response.getBody() != null && response.getBody().isSuccess()) {
                logger.info("Successfully retrieved address with id: {}", id);
            } else {
                logger.warn("Address not found with id: {}", id);
            }
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error fetching address with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EntityResponse<>("Failed to fetch address: " + e.getMessage(), false, null));
        }
    }

    /**
     * Retrieves all map points.
     *
     * @return ResponseEntity with EntityResponse containing a list of map points.
     *         - 200 OK with list of map points
     *         - 404 Not Found if no map points exist
     */
    @GetMapping("/map-points")
    @Operation(summary = "Get all map points")
    public ResponseEntity<EntityResponse<List<Location>>> getAllMapPoints() {
        try {
            logger.info("Fetching all map points");
            ResponseEntity<EntityResponse<List<Location>>> response = addressService.getAllMapPointsResponse();
            logger.info("Successfully retrieved map points");
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error fetching map points: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EntityResponse<>("Failed to fetch map points: " + e.getMessage(), false, null));
        }
    }

    /**
     * Adds a new address with map point.
     * Validates address details before creation.
     *
     * @param addressDTO Address DTO.
     * @return ResponseEntity with EntityResponse containing added address details.
     *         - 201 Created if address is created successfully
     *         - 400 Bad Request if validation fails
     *         - 403 Forbidden if user is not authorized
     */
    @PostMapping("/addresses")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add new address with map point")
    public ResponseEntity<EntityResponse<Address>> addAddress(@Valid @RequestBody AddressDTO addressDTO) {
        try {
            logger.info("Adding new address: {}", addressDTO);
            ResponseEntity<EntityResponse<Address>> response = addressService.addAddressResponse(addressDTO);
            if (response.getBody() != null && response.getBody().isSuccess()) {
                logger.info("Successfully added new address");
            } else {
                logger.warn("Failed to add new address");
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(response.getBody());
        } catch (Exception e) {
            logger.error("Error adding new address: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EntityResponse<>("Failed to add address: " + e.getMessage(), false, null));
        }
    }

    /**
     * Updates an existing address.
     * Verifies that the address belongs to the authenticated user.
     *
     * @param id        Address ID.
     * @param addressDTO Address DTO.
     * @return ResponseEntity with EntityResponse containing updated address details.
     *         - 200 OK if address updated successfully
     *         - 400 Bad Request if validation fails
     *         - 404 Not Found if address doesn't exist
     *         - 403 Forbidden if user doesn't own the address
     */
    @PutMapping("/addresses/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update existing address")
    public ResponseEntity<EntityResponse<Address>> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressDTO addressDTO) {
        try {
            logger.info("Updating address with id: {}", id);
            ResponseEntity<EntityResponse<Address>> response = addressService.updateAddressResponse(id, addressDTO);
            if (response.getBody() != null && response.getBody().isSuccess()) {
                logger.info("Successfully updated address with id: {}", id);
            } else {
                logger.warn("Failed to update address with id: {}", id);
            }
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error updating address with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EntityResponse<>("Failed to update address: " + e.getMessage(), false, null));
        }
    }

    /**
     * Deletes an address.
     * Verifies that the address belongs to the authenticated user.
     *
     * @param id Address ID.
     * @return ResponseEntity with EntityResponse containing deletion result.
     *         - 200 OK if address deleted successfully
     *         - 404 Not Found if address doesn't exist
     *         - 403 Forbidden if user doesn't own the address
     */
    @DeleteMapping("/addresses/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete address")
    public ResponseEntity<EntityResponse<Void>> deleteAddress(@PathVariable Long id) {
        try {
            logger.info("Deleting address with id: {}", id);
            ResponseEntity<EntityResponse<Void>> response = addressService.deleteAddressResponse(id);
            if (response.getBody() != null && response.getBody().isSuccess()) {
                logger.info("Successfully deleted address with id: {}", id);
            } else {
                logger.warn("Failed to delete address with id: {}", id);
            }
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error deleting address with id {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EntityResponse<>("Failed to delete address: " + e.getMessage(), false, null));
        }
    }

    /**
     * Searches addresses by city.
     *
     * @param city City name.
     * @return ResponseEntity with EntityResponse containing a list of addresses.
     *         - 200 OK with list of addresses
     *         - 404 Not Found if no addresses exist
     */
    @GetMapping("/addresses/search")
    @Operation(summary = "Search addresses by city")
    public ResponseEntity<EntityResponse<List<Address>>> searchAddresses(@RequestParam String city) {
        try {
            logger.info("Searching addresses in city: {}", city);
            ResponseEntity<EntityResponse<List<Address>>> response = addressService.searchAddressesByCityResponse(city);
            if (response.getBody() != null && response.getBody().getData() != null) {
                logger.info("Found {} addresses in city: {}", response.getBody().getData().size(), city);
            } else {
                logger.info("No addresses found in city: {}", city);
            }
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error searching addresses in city {}: {}", city, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EntityResponse<>("Failed to search addresses: " + e.getMessage(), false, null));
        }
    }

    /**
     * Finds the nearest address.
     *
     * @param latitude  Latitude.
     * @param longitude Longitude.
     * @return ResponseEntity with EntityResponse containing the nearest address.
     *         - 200 OK with nearest address
     *         - 404 Not Found if no addresses exist
     */
    @GetMapping("/addresses/nearest")
    @Operation(summary = "Find nearest address")
    public ResponseEntity<EntityResponse<Address>> findNearestAddress(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        try {
            logger.info("Finding nearest address to coordinates: {}, {}", latitude, longitude);
            ResponseEntity<EntityResponse<Address>> response = addressService.findNearestAddressResponse(latitude, longitude);
            if (response.getBody() != null && response.getBody().isSuccess()) {
                logger.info("Successfully found nearest address");
            } else {
                logger.warn("No nearest address found");
            }
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            logger.error("Error finding nearest address to coordinates {}, {}: {}", latitude, longitude, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new EntityResponse<>("Failed to find nearest address: " + e.getMessage(), false, null));
        }
    }
}