package uz.pdp.mapper;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import uz.pdp.dto.AddressDTO;
import uz.pdp.entity.Address;
import uz.pdp.enums.Socials;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-02-09T22:04:59+0500",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.41.0.z20250115-2156, environment: Java 21.0.5 (Eclipse Adoptium)"
)
@Component
public class AddressMapperImpl implements AddressMapper {

    @Override
    public AddressDTO toDto(Address address) {
        if ( address == null ) {
            return null;
        }

        AddressDTO addressDTO = new AddressDTO();

        addressDTO.setLocation( locationToDto( address.getLocation() ) );
        addressDTO.setCity( address.getCity() );
        addressDTO.setDefault( address.isDefault() );
        addressDTO.setEmail( address.getEmail() );
        addressDTO.setName( address.getName() );
        addressDTO.setPhoneNumber( address.getPhoneNumber() );
        Map<Socials, String> map = address.getSocialLinks();
        if ( map != null ) {
            addressDTO.setSocialLinks( new LinkedHashMap<Socials, String>( map ) );
        }
        addressDTO.setStreet( address.getStreet() );
        addressDTO.setWorkingHours( address.getWorkingHours() );

        return addressDTO;
    }

    @Override
    public Address toEntity(AddressDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Address address = new Address();

        address.setLocation( dtoToLocation( dto.getLocation() ) );
        address.setCity( dto.getCity() );
        address.setEmail( dto.getEmail() );
        address.setName( dto.getName() );
        address.setPhoneNumber( dto.getPhoneNumber() );
        Map<Socials, String> map = dto.getSocialLinks();
        if ( map != null ) {
            address.setSocialLinks( new LinkedHashMap<Socials, String>( map ) );
        }
        address.setStreet( dto.getStreet() );
        address.setWorkingHours( dto.getWorkingHours() );
        address.setDefault( dto.isDefault() );

        return address;
    }

    @Override
    public void updateEntityFromDto(AddressDTO dto, Address address) {
        if ( dto == null ) {
            return;
        }

        address.setLocation( dtoToLocation( dto.getLocation() ) );
        address.setCity( dto.getCity() );
        address.setEmail( dto.getEmail() );
        address.setName( dto.getName() );
        address.setPhoneNumber( dto.getPhoneNumber() );
        if ( address.getSocialLinks() != null ) {
            Map<Socials, String> map = dto.getSocialLinks();
            if ( map != null ) {
                address.getSocialLinks().clear();
                address.getSocialLinks().putAll( map );
            }
            else {
                address.setSocialLinks( null );
            }
        }
        else {
            Map<Socials, String> map = dto.getSocialLinks();
            if ( map != null ) {
                address.setSocialLinks( new LinkedHashMap<Socials, String>( map ) );
            }
        }
        address.setStreet( dto.getStreet() );
        address.setWorkingHours( dto.getWorkingHours() );
        address.setDefault( dto.isDefault() );
    }
}
