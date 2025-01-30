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
    date = "2025-01-30T14:37:38+0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.14 (Amazon.com Inc.)"
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
        addressDTO.setName( address.getName() );
        addressDTO.setStreet( address.getStreet() );
        addressDTO.setCity( address.getCity() );
        addressDTO.setPhoneNumber( address.getPhoneNumber() );
        addressDTO.setWorkingHours( address.getWorkingHours() );
        addressDTO.setEmail( address.getEmail() );
        addressDTO.setDefault( address.isDefault() );
        Map<Socials, String> map = address.getSocialLinks();
        if ( map != null ) {
            addressDTO.setSocialLinks( new LinkedHashMap<Socials, String>( map ) );
        }

        return addressDTO;
    }

    @Override
    public Address toEntity(AddressDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Address address = new Address();

        address.setLocation( dtoToLocation( dto.getLocation() ) );
        address.setDefault( dto.isDefault() );
        address.setName( dto.getName() );
        address.setStreet( dto.getStreet() );
        address.setCity( dto.getCity() );
        address.setPhoneNumber( dto.getPhoneNumber() );
        address.setWorkingHours( dto.getWorkingHours() );
        address.setEmail( dto.getEmail() );
        Map<Socials, String> map = dto.getSocialLinks();
        if ( map != null ) {
            address.setSocialLinks( new LinkedHashMap<Socials, String>( map ) );
        }

        return address;
    }

    @Override
    public void updateEntityFromDto(AddressDTO dto, Address address) {
        if ( dto == null ) {
            return;
        }

        address.setLocation( dtoToLocation( dto.getLocation() ) );
        address.setDefault( dto.isDefault() );
        address.setName( dto.getName() );
        address.setStreet( dto.getStreet() );
        address.setCity( dto.getCity() );
        address.setPhoneNumber( dto.getPhoneNumber() );
        address.setWorkingHours( dto.getWorkingHours() );
        address.setEmail( dto.getEmail() );
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
    }
}
