package uz.pdp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
 * Fun fact: This controller is like a phone book for doors! 📞🚪
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
     * For ADMIN users: Returns all addresses in the system
     * For other users: Returns only their own addresses
     * Like getting a directory of all the cool doors in town! 🏢
     *
     * @return EntityResponse containing a list of addresses
     */
    @GetMapping("/addresses")
    @Operation(summary = "Get all store addresses")
    public EntityResponse<List<Address>> getAllAddresses() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null && !authentication.getPrincipal().equals("anonymousUser") 
            ? authentication.getName() 
            : null;
        logger.info("Fetching store addresses. User: {}", username != null ? username : "anonymous");
        return addressService.getAllAddressesResponse(username);
    }

    /**
     * Retrieves address details by ID.
     * Finding that one special door in the haystack! 🔍
     *
     * @param id Address ID
     * @return EntityResponse containing address details
     */
    @GetMapping("/addresses/{id}")
    @Operation(summary = "Get store address by ID")
    public EntityResponse<Address> getAddress(@PathVariable Long id) {
        logger.info("Fetching store address with id: {}", id);
        return addressService.getAddressResponse(id);
    }

    /**
     * Retrieves all map points.
     * Mapping out our door empire! 🗺️
     *
     * @return EntityResponse containing a list of map points
     */
    @GetMapping("/addresses/map-points")
    @Operation(summary = "Get all store locations as map points")
    public EntityResponse<List<AddressDTO.LocationDTO>> getAllMapPoints() {
        logger.info("Fetching all store locations as map points");
        return addressService.getAllMapPointsResponse();
    }

    /**
     * Adds a new address with map point.
     * Building a new home for another door! 🏗️
     *
     * @param addressDTO Address DTO
     * @return EntityResponse containing added address details
     */
    @PostMapping("/addresses")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a new store address")
    public EntityResponse<Address> addAddress(@Valid @RequestBody AddressDTO addressDTO) {
        logger.info("Adding new store address: {}", addressDTO);
        return addressService.addAddressResponse(addressDTO);
    }

    /**
     * Updates an existing address.
     * Giving a door a fresh coat of paint! 🎨
     *
     * @param id Address ID
     * @param addressDTO Address DTO
     * @return EntityResponse containing updated address details
     */
    @PutMapping("/addresses/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing store address")
    public EntityResponse<Address> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressDTO addressDTO) {
        logger.info("Updating store address with id {}: {}", id, addressDTO);
        return addressService.updateAddressResponse(id, addressDTO);
    }

    /**
     * Deletes an address.
     * Saying goodbye to a faithful door! 👋
     *
     * @param id Address ID
     * @return EntityResponse containing deletion result
     */
    @DeleteMapping("/addresses/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a store address")
    public EntityResponse<Void> deleteAddress(@PathVariable Long id) {
        logger.info("Deleting store address with id: {}", id);
        return addressService.deleteAddressResponse(id);
    }

    /**
     * Searches addresses by city.
     * City-hopping from door to door! 🌆
     *
     * @param city City name
     * @return EntityResponse containing a list of addresses
     */
    @GetMapping("/addresses/search")
    @Operation(summary = "Search addresses by city")
    public EntityResponse<List<Address>> searchAddressesByCity(
            @RequestParam String city) {
        logger.info("Searching addresses in city: {}", city);
        return addressService.searchAddressesByCityResponse(city);
    }

    /**
     * Finds the nearest address.
     * Like a door-seeking missile! 🎯
     *
     * @param latitude Latitude
     * @param longitude Longitude
     * @return EntityResponse containing the nearest address
     */
    @GetMapping("/addresses/nearest")
    @Operation(summary = "Find nearest store address")
    public EntityResponse<Address> findNearestAddress(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        logger.info("Finding nearest address to coordinates: {}, {}", latitude, longitude);
        return addressService.findNearestAddressResponse(latitude, longitude);
    }
}