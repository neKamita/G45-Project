package uz.pdp.controller.graphql;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import uz.pdp.dto.AddressDTO;
import uz.pdp.entity.Address;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.AddressService;

import java.util.List;

/**
 * GraphQL controller for address operations.
 * Because REST is too mainstream and GraphQL is the cool kid now! 😎
 * 
 * Fun fact: This controller can find addresses faster than a GPS on steroids! 🗺️
 *
 * @version 1.0
 * @since 2025-01-17
 */
@Controller
public class AddressGraphQLController {
    private static final Logger logger = LoggerFactory.getLogger(AddressGraphQLController.class);
    private final AddressService addressService;

    public AddressGraphQLController(AddressService addressService) {
        this.addressService = addressService;
    }

    /**
     * Gets all addresses.
     * Like a phone book, but for doors! 📚
     */
    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public EntityResponse<List<Address>> getAllAddresses() {
        try {
            logger.info("GraphQL query: Retrieving all addresses");
            return addressService.getAllAddressesResponse();
        } catch (Exception e) {
            logger.error("Error retrieving addresses via GraphQL: {}", e.getMessage());
            return EntityResponse.error("Failed to retrieve addresses: " + e.getMessage(), null);
        }
    }

    /**
     * Gets an address by ID.
     * Finding addresses like a door-to-door salesman! 🚪
     */
    @QueryMapping
    public EntityResponse<Address> getAddress(@Argument Long id) {
        try {
            logger.info("GraphQL query: Retrieving address with ID: {}", id);
            return addressService.getAddressResponse(id);
        } catch (Exception e) {
            logger.error("Error retrieving address {} via GraphQL: {}", id, e.getMessage());
            return EntityResponse.error("Failed to retrieve address: " + e.getMessage(), null);
        }
    }

    /**
     * Creates a new address.
     * Building new homes for doors, one address at a time! 🏗️
     */
    @MutationMapping
    public EntityResponse<Address> createAddress(@Argument AddressDTO addressDTO) {
        try {
            logger.info("GraphQL mutation: Creating new address");
            return addressService.addAddressResponse(addressDTO);
        } catch (Exception e) {
            logger.error("Error creating address via GraphQL: {}", e.getMessage());
            return EntityResponse.error("Failed to create address: " + e.getMessage(), null);
        }
    }

    /**
     * Updates an address.
     * Because sometimes doors need a change of scenery! 🌅
     */
    @MutationMapping
    public EntityResponse<Address> updateAddress(
            @Argument Long id,
            @Argument AddressDTO addressDTO) {
        try {
            logger.info("GraphQL mutation: Updating address with ID: {}", id);
            return addressService.updateAddressResponse(id, addressDTO);
        } catch (Exception e) {
            logger.error("Error updating address {} via GraphQL: {}", id, e.getMessage());
            return EntityResponse.error("Failed to update address: " + e.getMessage(), null);
        }
    }

    /**
     * Deletes an address.
     * Saying goodbye to addresses, but never to memories! 👋
     */
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public EntityResponse<Void> deleteAddress(@Argument Long id) {
        try {
            logger.info("GraphQL mutation: Deleting address with ID: {}", id);
            return addressService.deleteAddressResponse(id);
        } catch (Exception e) {
            logger.error("Error deleting address {} via GraphQL: {}", id, e.getMessage());
            return EntityResponse.error("Failed to delete address: " + e.getMessage(), null);
        }
    }

    /**
     * Gets all map points.
     * Mapping doors like a treasure hunt! 🗺️
     */
    @QueryMapping
    public EntityResponse<List<AddressDTO.LocationDTO>> getAllMapPoints() {
        try {
            logger.info("GraphQL query: Retrieving all map points");
            return addressService.getAllMapPointsResponse();
        } catch (Exception e) {
            logger.error("Error retrieving map points via GraphQL: {}", e.getMessage());
            return EntityResponse.error("Failed to retrieve map points: " + e.getMessage(), null);
        }
    }

    /**
     * Searches addresses by city.
     * City-hopping like a door-to-door adventurer! 🌆
     */
    @QueryMapping
    public EntityResponse<List<Address>> searchAddressesByCity(@Argument String city) {
        try {
            logger.info("GraphQL query: Retrieving addresses for city: {}", city);
            return addressService.searchAddressesByCityResponse(city);
        } catch (Exception e) {
            logger.error("Error retrieving addresses for city {} via GraphQL: {}", city, e.getMessage());
            return EntityResponse.error("Failed to search addresses: " + e.getMessage(), null);
        }
    }

    /**
     * Finds the nearest address to given coordinates.
     * Like a GPS, but with more personality! 📍
     */
    @QueryMapping
    public EntityResponse<Address> findNearestAddress(
            @Argument Double latitude,
            @Argument Double longitude) {
        try {
            logger.info("GraphQL query: Finding nearest address to coordinates: lat={}, lon={}", latitude, longitude);
            return addressService.findNearestAddressResponse(latitude, longitude);
        } catch (Exception e) {
            logger.error("Error finding nearest address via GraphQL: {}", e.getMessage());
            return EntityResponse.error("Failed to find nearest address: " + e.getMessage(), null);
        }
    }
}