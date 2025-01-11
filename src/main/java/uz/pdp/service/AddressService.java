package uz.pdp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uz.pdp.repository.AddressRepository;
import uz.pdp.entity.Address;
import uz.pdp.entity.Location;
import uz.pdp.payload.EntityResponse;
import uz.pdp.dto.AddressDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AddressService {
    
    private static final Logger logger = LoggerFactory.getLogger(AddressService.class);
    
    @Autowired
    private AddressRepository addressRepository;

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
        updateAddressFromDTO(address, dto);
        return addressRepository.save(address);
    }

    public Address updateAddress(Long id, AddressDTO dto) {
        logger.info("Updating address with id: {}", id);
        Address address = getAddress(id);
        updateAddressFromDTO(address, dto);
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

    private void updateAddressFromDTO(Address address, AddressDTO dto) {
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
            List<Address> addresses = addressRepository.findAllByOrderByCity();
            return ResponseEntity.ok(EntityResponse.success(
                "Addresses retrieved successfully", addresses));
        } catch (Exception e) {
            logger.error("Error retrieving addresses: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(EntityResponse.error("Failed to retrieve addresses: " + e.getMessage()));
        }
    }

    public ResponseEntity<EntityResponse<Address>> getAddressResponse(Long id) {
        try {
            Address address = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + id));
            return ResponseEntity.ok(EntityResponse.success(
                "Address retrieved successfully", address));
        } catch (EntityNotFoundException e) {
            logger.error("Address not found: {}", e.getMessage());
            return ResponseEntity.status(404)  // Using direct HTTP status code instead of HttpStatus.NOT_FOUND
                .body(EntityResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error retrieving address: {}", e.getMessage());
            return ResponseEntity.status(400)  // Using direct HTTP status code instead of HttpStatus.BAD_REQUEST
                .body(EntityResponse.error("Failed to retrieve address: " + e.getMessage()));
        }
    }

    public ResponseEntity<EntityResponse<List<Location>>> getAllMapPointsResponse() {
        try {
            List<Location> points = getAllAddresses().stream()
                .map(Address::getLocation)
                .collect(Collectors.toList());
            return ResponseEntity.ok(EntityResponse.success(
                "Map points retrieved successfully", points));
        } catch (Exception e) {
            logger.error("Error retrieving map points: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(EntityResponse.error("Failed to retrieve map points: " + e.getMessage()));
        }
    }

    public ResponseEntity<EntityResponse<Address>> addAddressResponse(AddressDTO dto) {
        try {
            Address address = new Address();
            updateAddressFromDTO(address, dto);
            address = addressRepository.save(address);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(EntityResponse.success("Address added successfully", address));
        } catch (Exception e) {
            logger.error("Error adding address: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(EntityResponse.error("Failed to add address: " + e.getMessage()));
        }
    }

    public ResponseEntity<EntityResponse<Address>> updateAddressResponse(Long id, AddressDTO dto) {
        try {
            Address address = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + id));
            updateAddressFromDTO(address, dto);
            address = addressRepository.save(address);
            return ResponseEntity.ok(EntityResponse.success(
                "Address updated successfully", address));
        } catch (EntityNotFoundException e) {
            logger.error("Address not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(EntityResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating address: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(EntityResponse.error("Failed to update address: " + e.getMessage()));
        }
    }

    public ResponseEntity<EntityResponse<Void>> deleteAddressResponse(Long id) {
        try {
            if (!addressRepository.existsById(id)) {
                throw new EntityNotFoundException("Address not found with id: " + id);
            }
            addressRepository.deleteById(id);
            return ResponseEntity.ok(EntityResponse.success("Address deleted successfully"));
        } catch (EntityNotFoundException e) {
            logger.error("Address not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(EntityResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error deleting address: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(EntityResponse.error("Failed to delete address: " + e.getMessage()));
        }
    }

    public ResponseEntity<EntityResponse<List<Address>>> searchAddressesByCityResponse(String city) {
        try {
            List<Address> addresses = addressRepository.findByCityContainingIgnoreCase(city);
            String message = addresses.isEmpty() 
                ? String.format("No addresses found in city: %s", city)
                : String.format("Found %d addresses in %s", addresses.size(), city);
            return ResponseEntity.ok(EntityResponse.success(message, addresses));
        } catch (Exception e) {
            logger.error("Error searching addresses: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(EntityResponse.error("Failed to search addresses: " + e.getMessage()));
        }
    }

}
