package uz.pdp.controller.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import uz.pdp.entity.Address;
import uz.pdp.payload.EntityResponse;
import uz.pdp.service.AddressService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * GraphQL controller for address-related operations.
 * Handles queries and mutations for managing user and door addresses.
 * Implements security controls for address management.
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
     * GraphQL query to retrieve all addresses.
     * Requires admin privileges.
     *
     * @return List of all addresses in the system
     */
    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Address> getAllAddresses() {
        try {
            logger.info("GraphQL query: Retrieving all addresses");
            List<Address> addresses = addressService.getAllAddresses();
            logger.info("Retrieved {} addresses", addresses.size());
            return addresses;
        } catch (Exception e) {
            logger.error("Error retrieving addresses via GraphQL: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * GraphQL query to retrieve an address by ID.
     *
     * @param id ID of the address to retrieve
     * @return Address details if found
     */
    @QueryMapping
    public Address getAddressById(@Argument Long id) {
        try {
            logger.info("GraphQL query: Retrieving address with ID: {}", id);
            Address address = addressService.getAddressById(id);
            logger.info("Retrieved address: {}", address.getStreet());
            return address;
        } catch (Exception e) {
            logger.error("Error retrieving address {} via GraphQL: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * GraphQL query to retrieve addresses by city.
     *
     * @param city Name of the city
     * @return List of addresses in the specified city
     */
    @QueryMapping
    public List<Address> getAddressesByCity(@Argument String city) {
        try {
            logger.info("GraphQL query: Retrieving addresses for city: {}", city);
            List<Address> addresses = addressService.getAddressesByCity(city);
            logger.info("Retrieved {} addresses for city {}", addresses.size(), city);
            return addresses;
        } catch (Exception e) {
            logger.error("Error retrieving addresses for city {} via GraphQL: {}", city, e.getMessage());
            throw e;
        }
    }

    /**
     * GraphQL mutation to create a new address.
     *
     * @param address Address details to create
     * @return Created address information
     */
    @MutationMapping
    public Address createAddress(@Argument Address address) {
        try {
            logger.info("GraphQL mutation: Creating new address");
            EntityResponse<Address> response = addressService.createAddress(address);
            logger.info("Created address: {}", response.getData().getStreet());
            return response.getData();
        } catch (Exception e) {
            logger.error("Error creating address via GraphQL: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * GraphQL mutation to update an existing address.
     *
     * @param id ID of the address to update
     * @param address Updated address details
     * @return Updated address information
     */
    @MutationMapping
    public Address updateAddress(@Argument Long id, @Argument Address address) {
        try {
            logger.info("GraphQL mutation: Updating address with ID: {}", id);
            EntityResponse<Address> response = addressService.updateAddress(id, address);
            logger.info("Updated address: {}", response.getData().getStreet());
            return response.getData();
        } catch (Exception e) {
            logger.error("Error updating address {} via GraphQL: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * GraphQL mutation to delete an address.
     * Requires admin privileges.
     *
     * @param id ID of the address to delete
     * @return true if deletion successful
     */
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public boolean deleteAddress(@Argument Long id) {
        try {
            logger.info("GraphQL mutation: Deleting address with ID: {}", id);
            EntityResponse<Void> response = addressService.deleteAddress(id);
            logger.info("Address {} deleted successfully", id);
            return response.isSuccess();
        } catch (Exception e) {
            logger.error("Error deleting address {} via GraphQL: {}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * GraphQL mutation to validate an address.
     * Checks if the address exists and is properly formatted.
     *
     * @param address Address to validate
     * @return true if address is valid
     */
    @MutationMapping
    public boolean validateAddress(@Argument Address address) {
        try {
            logger.info("GraphQL mutation: Validating address");
            boolean isValid = addressService.validateAddress(address);
            logger.info("Address validation result: {}", isValid);
            return isValid;
        } catch (Exception e) {
            logger.error("Error validating address via GraphQL: {}", e.getMessage());
            throw e;
        }
    }
}