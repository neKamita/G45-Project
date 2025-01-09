package uz.pdp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uz.pdp.repository.AddressRepository;
import uz.pdp.entity.Address;
import uz.pdp.entity.MapPoint;
import uz.pdp.dto.AddressDTO;

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

    public List<MapPoint> getAllMapPoints() {
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

        MapPoint point = address.getLocation();
        if (point == null) {
            point = new MapPoint();
            point.setAddress(address);
        }
        point.setLatitude(dto.getLatitude());
        point.setLongitude(dto.getLongitude());
        point.setMarkerTitle(dto.getName());
        
        address.setLocation(point);
    }

    public Address findNearestAddress(Double latitude, Double longitude) {
        logger.info("Finding nearest address to coordinates: {}, {}", latitude, longitude);
        return getAllAddresses().stream()
            .min((a1, a2) -> compareDistances(a1, a2, latitude, longitude))
            .orElseThrow(() -> new EntityNotFoundException("No addresses found"));
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
}
