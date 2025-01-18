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
        logger.info("Fetching all store addresses");
        return addressService.getAllAddressesResponse();
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
    @Operation(summary = "Get store address by ID")
    public ResponseEntity<EntityResponse<Address>> getAddress(@PathVariable Long id) {
        logger.info("Fetching store address with id: {}", id);
        return addressService.getAddressResponse(id);
    }

    /**
     * Retrieves all map points.
     *
     * @return ResponseEntity with EntityResponse containing a list of map points.
     *         - 200 OK with list of map points
     *         - 404 Not Found if no map points exist
     */
    @GetMapping("/addresses/map-points")
    @Operation(summary = "Get all store locations as map points")
    public ResponseEntity<EntityResponse<List<AddressDTO.LocationDTO>>> getAllMapPoints() {
        logger.info("Fetching all store locations as map points");
        return addressService.getAllMapPointsResponse();
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
    @Operation(summary = "Add a new store address")
    public ResponseEntity<EntityResponse<Address>> addAddress(@Valid @RequestBody AddressDTO addressDTO) {
        logger.info("Adding new store address: {}", addressDTO);
        return addressService.addAddressResponse(addressDTO);
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
    @Operation(summary = "Update an existing store address")
    public ResponseEntity<EntityResponse<Address>> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressDTO addressDTO) {
        logger.info("Updating store address with id {}: {}", id, addressDTO);
        return addressService.updateAddressResponse(id, addressDTO);
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
    @Operation(summary = "Delete a store address")
    public ResponseEntity<EntityResponse<Void>> deleteAddress(@PathVariable Long id) {
        logger.info("Deleting store address with id: {}", id);
        return addressService.deleteAddressResponse(id);
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
    public ResponseEntity<EntityResponse<List<Address>>> searchAddressesByCity(
            @RequestParam String city) {
        logger.info("Searching addresses in city: {}", city);
        return addressService.searchAddressesByCityResponse(city);
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
    @Operation(summary = "Find nearest store address")
    public ResponseEntity<EntityResponse<Address>> findNearestAddress(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        logger.info("Finding nearest address to coordinates: {}, {}", latitude, longitude);
        return addressService.findNearestAddressResponse(latitude, longitude);
    }
}