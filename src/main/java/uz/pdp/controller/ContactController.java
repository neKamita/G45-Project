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
import org.springframework.http.converter.HttpMessageNotReadableException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import uz.pdp.dto.AddressDTO;
import uz.pdp.entity.Address;
import uz.pdp.entity.Location;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.AddressService;

@RestController
@RequestMapping("/api/contacts")
@Tag(name = "Contact Management", description = "APIs for managing contact addresses and map points")
public class    ContactController {
    
    private static final Logger logger = LoggerFactory.getLogger(ContactController.class);
    
    @Autowired
    private AddressService addressService;

    @GetMapping("/addresses")
    @Operation(summary = "Get all store addresses")
    public ResponseEntity<EntityResponse<List<Address>>> getAllAddresses() {
        logger.info("Fetching all store addresses");
        return addressService.getAllAddressesResponse();
    }

    @GetMapping("/addresses/{id}")
    @Operation(summary = "Get address details by ID")
    public ResponseEntity<EntityResponse<Address>> getAddress(@PathVariable Long id) {
        logger.info("Fetching address with id: {}", id);
        return addressService.getAddressResponse(id);
    }

    @GetMapping("/map-points")
    @Operation(summary = "Get all map points")
    public ResponseEntity<EntityResponse<List<Location>>> getAllMapPoints() {
        logger.info("Fetching all map points");
        return addressService.getAllMapPointsResponse();
    }

    @PostMapping("/addresses")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add new address with map point")
    public ResponseEntity<EntityResponse<Address>> addAddress(@Valid @RequestBody AddressDTO addressDTO) {
        logger.info("Adding new address: {}", addressDTO);
        return addressService.addAddressResponse(addressDTO);
    }

    @PutMapping("/addresses/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update existing address")
    public ResponseEntity<EntityResponse<Address>> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressDTO addressDTO) {
        logger.info("Updating address with id: {}", id);
        return addressService.updateAddressResponse(id, addressDTO);
    }

    @DeleteMapping("/addresses/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete address")
    public ResponseEntity<EntityResponse<Void>> deleteAddress(@PathVariable Long id) {
        logger.info("Deleting address with id: {}", id);
        return addressService.deleteAddressResponse(id);
    }

    @GetMapping("/addresses/search")
    @Operation(summary = "Search addresses by city")
    public ResponseEntity<EntityResponse<List<Address>>> searchAddresses(@RequestParam String city) {
        logger.info("Searching addresses in city: {}", city);
        return addressService.searchAddressesByCityResponse(city);
    }

    @GetMapping("/addresses/nearest")
    @Operation(summary = "Find nearest address")
    public ResponseEntity<EntityResponse<Address>> findNearestAddress(
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        logger.info("Finding nearest address to coordinates: {}, {}", latitude, longitude);
        return addressService.findNearestAddressResponse(latitude, longitude);
    }
}
