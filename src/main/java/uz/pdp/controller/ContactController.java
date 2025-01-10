package uz.pdp.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import uz.pdp.dto.AddressDTO;
import uz.pdp.entity.Address;
import uz.pdp.entity.MapPoint;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.AddressService;

@RestController
@RequestMapping("/api/contacts")
@Tag(name = "Contact Management", description = "APIs for managing contact addresses and map points")
public class ContactController {
    
    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);
    
    @Autowired
    private AddressService addressService;

    @GetMapping("/addresses")
    @Operation(summary = "Get all store addresses")
    public ResponseEntity<EntityResponse<List<Address>>> getAllAddresses() {
        logger.info("Fetching all store addresses");
        List<Address> addresses = addressService.getAllAddresses();
        logger.debug("Retrieved {} addresses", addresses.size());
        return ResponseEntity.ok(EntityResponse.success("Addresses retrieved successfully", addresses));
    }

    @GetMapping("/addresses/{id}")
    @Operation(summary = "Get address details by ID")
    public ResponseEntity<EntityResponse<Address>> getAddress(@PathVariable Long id) {
        logger.info("Fetching address with id: {}", id);
        Address address = addressService.getAddress(id);
        logger.debug("Retrieved address: {}", address);
        return ResponseEntity.ok(EntityResponse.success("Address retrieved successfully", address));
    }

    @GetMapping("/map-points")
    @Operation(summary = "Get all map points")
    public ResponseEntity<EntityResponse<List<MapPoint>>> getAllMapPoints() {
        logger.info("Fetching all map points");
        List<MapPoint> points = addressService.getAllMapPoints();
        logger.debug("Retrieved {} map points", points.size());
        return ResponseEntity.ok(EntityResponse.success("Map points retrieved successfully", points));
    }

    @PostMapping("/addresses")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add new address with map point")
    public ResponseEntity<EntityResponse<Address>> addAddress(@Valid @RequestBody AddressDTO addressDTO) {
        logger.info("Adding new address: {}", addressDTO);
        try {
            Address address = addressService.addAddress(addressDTO);
            logger.info("Successfully added address with id: {}", address.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(EntityResponse.success("Address added successfully", address));
        } catch (Exception e) {
            logger.error("Error adding address: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(EntityResponse.error("Failed to add address: " + e.getMessage()));
        }
    }

    @PutMapping("/addresses/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update existing address")
    public ResponseEntity<EntityResponse<Address>> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressDTO addressDTO) {
        logger.info("Updating address with id: {}", id);
        try {
            Address address = addressService.updateAddress(id, addressDTO);
            logger.info("Successfully updated address with id: {}", id);
            return ResponseEntity.ok(EntityResponse.success("Address updated successfully", address));
        } catch (Exception e) {
            logger.error("Error updating address: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(EntityResponse.error("Failed to update address: " + e.getMessage()));
        }
    }

    @DeleteMapping("/addresses/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete address")
    public ResponseEntity<EntityResponse<Void>> deleteAddress(@PathVariable Long id) {
        logger.info("Deleting address with id: {}", id);
        try {
            addressService.deleteAddress(id);
            logger.info("Successfully deleted address with id: {}", id);
            return ResponseEntity.ok(EntityResponse.success("Address deleted successfully"));
        } catch (Exception e) {
            logger.error("Error deleting address: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(EntityResponse.error("Failed to delete address: " + e.getMessage()));
        }
    }

    @GetMapping("/addresses/search")
    @Operation(summary = "Search addresses by city")
    public ResponseEntity<EntityResponse<List<Address>>> searchAddresses(@RequestParam String city) {
        logger.info("Searching addresses in city: {}", city);
        List<Address> addresses = addressService.searchAddressesByCity(city);
        logger.debug("Found {} addresses in {}", addresses.size(), city);
        return ResponseEntity.ok(EntityResponse.success("Addresses found successfully", addresses));
    }

    @GetMapping("/addresses/nearest")
    @Operation(summary = "Find nearest address")
    public ResponseEntity<EntityResponse<Address>> findNearestAddress(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        logger.info("Finding nearest address to coordinates: {}, {}", latitude, longitude);
        Address address = addressService.findNearestAddress(latitude, longitude);
        return ResponseEntity.ok(EntityResponse.success("Nearest address found successfully", address));
    }
}
