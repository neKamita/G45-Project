package uz.pdp.mapper;

import org.mapstruct.*;
import uz.pdp.dto.AddressDTO;
import uz.pdp.dto.*; // Making sure our address gets its proper door to the DTO party! 🚪✨
import uz.pdp.entity.Address;
import uz.pdp.entity.Location;

/**
 * Mapper for converting between Address entities and DTOs.
 * 
 * Fun fact: This mapper is like a GPS for your doors - 
 * it knows exactly where they live! 🏠✨
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AddressMapper {

    /**
     * Converts an Address entity to an AddressDTO.
     * 
     * @param address The address entity to convert
     * @return The converted DTO
     * 
     * Like getting the perfect directions to your door! 🗺️
     */
    @Mapping(target = "location", source = "location", qualifiedByName = "locationToDto")
    AddressDTO toDto(Address address);

    /**
     * Converts an AddressDTO to an Address entity.
     * 
     * @param dto The DTO to convert
     * @return The converted entity
     * 
     * Time to put that door on the map! 📍
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "location", source = "location", qualifiedByName = "dtoToLocation")
    Address toEntity(AddressDTO dto);

    /**
     * Updates an existing Address entity with DTO data.
     * 
     * @param dto The source DTO
     * @param address The address entity to update
     * 
     * Like updating your door's delivery address! 📦
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "location", source = "location", qualifiedByName = "dtoToLocation")
    void updateEntityFromDto(AddressDTO dto, @MappingTarget Address address);

    /**
     * Converts a Location entity to LocationDTO.
     */
    @Named("locationToDto")
    default AddressDTO.LocationDTO locationToDto(Location location) {
        if (location == null) return null;
        return new AddressDTO.LocationDTO(
            location.getLatitude(),
            location.getLongitude(),
            location.getMarkerTitle()
        );
    }

    /**
     * Converts a LocationDTO to Location entity.
     */
    @Named("dtoToLocation")
    default Location dtoToLocation(AddressDTO.LocationDTO dto) {
        if (dto == null) return null;
        Location location = new Location();
        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());
        location.setMarkerTitle(dto.getMarkerTitle());
        return location;
    }
}
