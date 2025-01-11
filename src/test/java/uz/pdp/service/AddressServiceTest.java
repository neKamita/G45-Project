package uz.pdp.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;
import uz.pdp.dto.AddressDTO;
import uz.pdp.entity.Address;
import uz.pdp.entity.Location;
import uz.pdp.entity.MapPoint;
import uz.pdp.repository.AddressRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressService addressService;

    @Test
    void getAllAddresses_Success() {
        // Arrange
        List<Address> addresses = Arrays.asList(
            createTestAddress(1L, "Store 1", "City 1"),
            createTestAddress(2L, "Store 2", "City 2")
        );
        when(addressRepository.findAllByOrderByCity()).thenReturn(addresses);

        // Act
        List<Address> result = addressService.getAllAddresses();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Store 1", result.get(0).getName());
        verify(addressRepository).findAllByOrderByCity();
    }


    @Test
    void getAddress_Success() {
        // Arrange
        Long id = 1L;
        Address address = createTestAddress(id, "Test Store", "Test City");
        when(addressRepository.findById(id)).thenReturn(Optional.of(address));

        // Act
        Address result = addressService.getAddress(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Test Store", result.getName());
        verify(addressRepository).findById(id);
    }

    @Test
    void getAddress_NotFound() {
        // Arrange
        Long id = 1L;
        when(addressRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> addressService.getAddress(id));
        verify(addressRepository).findById(id);
    }

    @Test
    void addAddress_Success() {
        // Arrange
        AddressDTO dto = createTestAddressDTO();
        Address savedAddress = createTestAddress(1L, dto.getName(), dto.getCity());
        when(addressRepository.save(any(Address.class))).thenReturn(savedAddress);

        // Act
        Address result = addressService.addAddress(dto);

        // Assert
        assertNotNull(result);
        assertEquals(dto.getName(), result.getName());
        assertEquals(dto.getCity(), result.getCity());
        verify(addressRepository).save(any(Address.class));
    }

    @Test
    void searchAddressesByCity_Success() {
        // Arrange
        String city = "Test City";
        List<Address> addresses = Arrays.asList(
            createTestAddress(1L, "Store 1", city),
            createTestAddress(2L, "Store 2", city)
        );
        when(addressRepository.findByCityContainingIgnoreCase(city)).thenReturn(addresses);

        // Act
        List<Address> result = addressService.searchAddressesByCity(city);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(a -> a.getCity().equals(city)));
        verify(addressRepository).findByCityContainingIgnoreCase(city);
    }

    @Test
    void findNearestAddress_Success() {
        // Arrange
        Double lat = 41.123;
        Double lon = 69.456;
        List<Address> addresses = Arrays.asList(
            createTestAddressWithLocation(1L, "Store 1", 41.124, 69.457),
            createTestAddressWithLocation(2L, "Store 2", 41.125, 69.458)
        );
        when(addressRepository.findAllByOrderByCity()).thenReturn(addresses);

        // Act
        Address result = addressService.findNearestAddress(lat, lon);

        // Assert
        assertNotNull(result);
        assertEquals("Store 1", result.getName());
        verify(addressRepository).findAllByOrderByCity();
    }

    // Helper methods
    private Address createTestAddress(Long id, String name, String city) {
        Address address = new Address();
        address.setId(id);
        address.setName(name);
        address.setCity(city);
        address.setStreet("Test Street");
        address.setPhone("+1234567890");
        return address;
    }

    private Address createTestAddressWithLocation(Long id, String name, Double lat, Double lon) {
        Address address = createTestAddress(id, name, "Test City");
        Location location = new Location();
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setMarkerTitle(name);
        location.setAddress(address);
        address.setLocation(location);
        return address;
    }

    private AddressDTO createTestAddressDTO() {
        AddressDTO dto = new AddressDTO();
        dto.setName("Test Store");
        dto.setStreet("Test Street");
        dto.setCity("Test City");
        dto.setPhone("+1234567890");
        dto.setLatitude(41.123);
        dto.setLongitude(69.456);
        return dto;
    }
}
