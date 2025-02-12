package uz.pdp.mapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import uz.pdp.dto.DoorDto;
import uz.pdp.dto.DoorResponseDTO;
import uz.pdp.entity.Door;
import uz.pdp.enums.Color;
import uz.pdp.enums.DoorStatus;
import uz.pdp.enums.Size;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-02-12T13:48:59+0500",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.41.0.z20250115-2156, environment: Java 21.0.5 (Eclipse Adoptium)"
)
@Component
public class DoorMapperImpl implements DoorMapper {

    @Override
    public DoorDto toDto(Door door) {
        if ( door == null ) {
            return null;
        }

        DoorDto doorDto = new DoorDto();

        Set<Color> set = door.getAvailableColors();
        if ( set != null ) {
            doorDto.setAvailableColors( new LinkedHashSet<Color>( set ) );
        }
        Set<Size> set1 = door.getAvailableSizes();
        if ( set1 != null ) {
            doorDto.setAvailableSizes( new LinkedHashSet<Size>( set1 ) );
        }
        doorDto.setColor( door.getColor() );
        doorDto.setCustomHeight( door.getCustomHeight() );
        doorDto.setCustomWidth( door.getCustomWidth() );
        doorDto.setDescription( door.getDescription() );
        doorDto.setDoorLocation( door.getDoorLocation() );
        doorDto.setFinalPrice( door.getFinalPrice() );
        doorDto.setFrameType( door.getFrameType() );
        doorDto.setHardware( door.getHardware() );
        doorDto.setId( door.getId() );
        List<String> list = door.getImages();
        if ( list != null ) {
            doorDto.setImages( new ArrayList<String>( list ) );
        }
        doorDto.setIsCustomColor( door.getIsCustomColor() );
        doorDto.setManufacturer( door.getManufacturer() );
        doorDto.setMaterial( door.getMaterial() );
        doorDto.setName( door.getName() );
        doorDto.setPrice( door.getPrice() );
        doorDto.setSize( door.getSize() );
        doorDto.setWarrantyYears( door.getWarrantyYears() );

        doorDto.setStatus( door.getStatus().toString() );
        doorDto.setCategoryName( door.getCategory() != null ? door.getCategory().getName() : null );

        return doorDto;
    }

    @Override
    public Door toEntity(DoorDto dto) {
        if ( dto == null ) {
            return null;
        }

        Door door = new Door();

        Set<Size> set = dto.getAvailableSizes();
        if ( set != null ) {
            door.setAvailableSizes( new LinkedHashSet<Size>( set ) );
        }
        door.setColor( dto.getColor() );
        door.setCustomHeight( dto.getCustomHeight() );
        door.setCustomWidth( dto.getCustomWidth() );
        door.setDescription( dto.getDescription() );
        door.setDoorLocation( dto.getDoorLocation() );
        door.setFinalPrice( dto.getFinalPrice() );
        door.setFrameType( dto.getFrameType() );
        door.setHardware( dto.getHardware() );
        List<String> list = dto.getImages();
        if ( list != null ) {
            door.setImages( new ArrayList<String>( list ) );
        }
        door.setIsCustomColor( dto.getIsCustomColor() );
        door.setManufacturer( dto.getManufacturer() );
        door.setMaterial( dto.getMaterial() );
        door.setName( dto.getName() );
        door.setPrice( dto.getPrice() );
        door.setWarrantyYears( dto.getWarrantyYears() );
        door.setSize( dto.getSize() );

        door.setActive( true );
        door.setStatus( DoorStatus.AVAILABLE );
        door.setIsBaseModel( false );

        afterMapping( door );

        return door;
    }

    @Override
    public void updateEntityFromDto(DoorDto dto, Door door) {
        if ( dto == null ) {
            return;
        }

        if ( door.getAvailableSizes() != null ) {
            Set<Size> set = dto.getAvailableSizes();
            if ( set != null ) {
                door.getAvailableSizes().clear();
                door.getAvailableSizes().addAll( set );
            }
            else {
                door.setAvailableSizes( null );
            }
        }
        else {
            Set<Size> set = dto.getAvailableSizes();
            if ( set != null ) {
                door.setAvailableSizes( new LinkedHashSet<Size>( set ) );
            }
        }
        door.setColor( dto.getColor() );
        door.setCustomHeight( dto.getCustomHeight() );
        door.setCustomWidth( dto.getCustomWidth() );
        door.setDescription( dto.getDescription() );
        door.setDoorLocation( dto.getDoorLocation() );
        door.setFinalPrice( dto.getFinalPrice() );
        door.setFrameType( dto.getFrameType() );
        door.setHardware( dto.getHardware() );
        if ( door.getImages() != null ) {
            List<String> list = dto.getImages();
            if ( list != null ) {
                door.getImages().clear();
                door.getImages().addAll( list );
            }
            else {
                door.setImages( null );
            }
        }
        else {
            List<String> list = dto.getImages();
            if ( list != null ) {
                door.setImages( new ArrayList<String>( list ) );
            }
        }
        door.setIsCustomColor( dto.getIsCustomColor() );
        door.setManufacturer( dto.getManufacturer() );
        door.setMaterial( dto.getMaterial() );
        door.setName( dto.getName() );
        door.setPrice( dto.getPrice() );
        door.setWarrantyYears( dto.getWarrantyYears() );
        door.setSize( dto.getSize() );

        afterMapping( door );
    }

    @Override
    public DoorResponseDTO toResponseDto(Door door) {
        if ( door == null ) {
            return null;
        }

        DoorResponseDTO.DoorResponseDTOBuilder doorResponseDTO = DoorResponseDTO.builder();

        doorResponseDTO.color( door.getColor() );
        doorResponseDTO.description( door.getDescription() );
        doorResponseDTO.id( door.getId() );
        List<String> list = door.getImages();
        if ( list != null ) {
            doorResponseDTO.images( new ArrayList<String>( list ) );
        }
        doorResponseDTO.name( door.getName() );
        if ( door.getPrice() != null ) {
            doorResponseDTO.price( BigDecimal.valueOf( door.getPrice() ) );
        }
        doorResponseDTO.status( door.getStatus() );

        doorResponseDTO.category( mapCategory(door.getCategory()) );

        return doorResponseDTO.build();
    }
}
