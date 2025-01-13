package uz.pdp.controller.graphql;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import uz.pdp.dto.AddressDTO;
import uz.pdp.entity.Address;
import uz.pdp.mutations.AddressConfigInput;
import uz.pdp.service.AddressService;


@Controller
public class AddressGraphQLController {
    private final AddressService addressService;
    private static final Logger logger = LoggerFactory.getLogger(AddressGraphQLController.class);

    @Autowired
    public AddressGraphQLController(AddressService addressService) {
        this.addressService = addressService;
    }

    @QueryMapping
    public Address address(@Argument String id) {  // Changed ID to String
        logger.info("GraphQL Query: Fetching address {}", id);
        return addressService.getAddress(Long.valueOf(id));
    }

    @QueryMapping
    public List<Address> addresses() {
        logger.info("GraphQL Query: Fetching all addresses");
        return addressService.getAllAddresses();
    }

    @QueryMapping
    public List<Address> searchAddresses(@Argument String city) {
        logger.info("GraphQL Query: Searching addresses in {}", city);
        return addressService.searchAddressesByCity(city);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Address createAddress(@Argument("input") AddressConfigInput input) {
        logger.info("GraphQL Mutation: Creating address");
        AddressDTO dto = new AddressDTO();
        dto.setName(input.getName());
        dto.setStreet(input.getStreet());
        dto.setCity(input.getCity());
        dto.setPhone(input.getPhone());
        dto.setWorkingHours(input.getWorkingHours());
        dto.setEmail(input.getEmail());
        dto.setLatitude(input.getLatitude());
        dto.setLongitude(input.getLongitude());
        dto.setSocialLinks(input.getSocialLinks());
        return addressService.addAddress(dto);
    }
}