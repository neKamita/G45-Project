package uz.pdp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import uz.pdp.dto.AddressDTO;
import uz.pdp.entity.Address;
import uz.pdp.service.AddressService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ContactControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AddressService addressService;

    @InjectMocks
    private ContactController contactController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(contactController)
            .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAllAddresses_Success() throws Exception {
        // Arrange
        List<Address> addresses = Arrays.asList(
            createTestAddress(1L, "Store 1", "City 1"),
            createTestAddress(2L, "Store 2", "City 2")
        );
        when(addressService.getAllAddresses()).thenReturn(addresses);

        // Act & Assert
        mockMvc.perform(get("/api/contacts/addresses")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    void getAddress_Success() throws Exception {
        // Arrange
        Address address = createTestAddress(1L, "Test Store", "Test City");
        when(addressService.getAddress(1L)).thenReturn(address);

        // Act & Assert
        mockMvc.perform(get("/api/contacts/addresses/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("Test Store"));
    }

    @Test
    void addAddress_Success() throws Exception {
        // Arrange
        AddressDTO dto = createTestAddressDTO();
        Address savedAddress = createTestAddress(1L, dto.getName(), dto.getCity());
        when(addressService.addAddress(any(AddressDTO.class))).thenReturn(savedAddress);

        // Act & Assert
        mockMvc.perform(post("/api/contacts/addresses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateAddress_Success() throws Exception {
        // Arrange
        AddressDTO dto = createTestAddressDTO();
        Address updatedAddress = createTestAddress(1L, dto.getName(), dto.getCity());
        when(addressService.updateAddress(eq(1L), any(AddressDTO.class))).thenReturn(updatedAddress);

        // Act & Assert
        mockMvc.perform(put("/api/contacts/addresses/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteAddress_Success() throws Exception {
        // Arrange
        doNothing().when(addressService).deleteAddress(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/contacts/addresses/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void searchAddresses_Success() throws Exception {
        // Arrange
        List<Address> addresses = Arrays.asList(
            createTestAddress(1L, "Store 1", "Test City"),
            createTestAddress(2L, "Store 2", "Test City")
        );
        when(addressService.searchAddressesByCity("Test City")).thenReturn(addresses);

        // Act & Assert
        mockMvc.perform(get("/api/contacts/addresses/search")
                .param("city", "Test City"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data", hasSize(2)));
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
