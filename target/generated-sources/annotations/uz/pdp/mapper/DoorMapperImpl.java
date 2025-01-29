package uz.pdp.mapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import uz.pdp.dto.DoorDto;
import uz.pdp.dto.DoorResponseDTO;
import uz.pdp.entity.Door;
import uz.pdp.enums.DoorStatus;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-01-29T18:24:14+0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.14 (Amazon.com Inc.)"
)
@Component
public class DoorMapperImpl implements DoorMapper {

    @Override
    public DoorDto toDto(Door door) {
        if ( door == null ) {
            return null;
        }

        DoorDto doorDto = new DoorDto();

        doorDto.setId( door.getId() );
        doorDto.setName( door.getName() );
        doorDto.setDescription( door.getDescription() );
        doorDto.setPrice( door.getPrice() );
        doorDto.setSize( door.getSize() );
        doorDto.setColor( door.getColor() );
        doorDto.setMaterial( door.getMaterial() );
        doorDto.setManufacturer( door.getManufacturer() );
        doorDto.setFrameType( door.getFrameType() );
        doorDto.setHardware( door.getHardware() );
        doorDto.setDoorLocation( door.getDoorLocation() );
        doorDto.setWarrantyYears( door.getWarrantyYears() );
        doorDto.setCustomWidth( door.getCustomWidth() );
        doorDto.setCustomHeight( door.getCustomHeight() );
        doorDto.setIsCustomColor( door.getIsCustomColor() );
        doorDto.setFinalPrice( door.getFinalPrice() );
        List<String> list = door.getImages();
        if ( list != null ) {
            doorDto.setImages( new ArrayList<String>( list ) );
        }

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

        door.setName( dto.getName() );
        door.setDescription( dto.getDescription() );
        door.setPrice( dto.getPrice() );
        door.setFinalPrice( dto.getFinalPrice() );
        List<String> list = dto.getImages();
        if ( list != null ) {
            door.setImages( new ArrayList<String>( list ) );
        }
        door.setSize( dto.getSize() );
        door.setColor( dto.getColor() );
        door.setMaterial( dto.getMaterial() );
        door.setManufacturer( dto.getManufacturer() );
        door.setWarrantyYears( dto.getWarrantyYears() );
        door.setCustomWidth( dto.getCustomWidth() );
        door.setCustomHeight( dto.getCustomHeight() );
        door.setIsCustomColor( dto.getIsCustomColor() );
        door.setDoorLocation( dto.getDoorLocation() );
        door.setFrameType( dto.getFrameType() );
        door.setHardware( dto.getHardware() );

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

        door.setName( dto.getName() );
        door.setDescription( dto.getDescription() );
        door.setPrice( dto.getPrice() );
        door.setFinalPrice( dto.getFinalPrice() );
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
        door.setSize( dto.getSize() );
        door.setColor( dto.getColor() );
        door.setMaterial( dto.getMaterial() );
        door.setManufacturer( dto.getManufacturer() );
        door.setWarrantyYears( dto.getWarrantyYears() );
        door.setCustomWidth( dto.getCustomWidth() );
        door.setCustomHeight( dto.getCustomHeight() );
        door.setIsCustomColor( dto.getIsCustomColor() );
        door.setDoorLocation( dto.getDoorLocation() );
        door.setFrameType( dto.getFrameType() );
        door.setHardware( dto.getHardware() );

        afterMapping( door );
    }

    @Override
    public DoorResponseDTO toResponseDto(Door door) {
        if ( door == null ) {
            return null;
        }

        DoorResponseDTO.DoorResponseDTOBuilder doorResponseDTO = DoorResponseDTO.builder();

        doorResponseDTO.id( door.getId() );
        doorResponseDTO.name( door.getName() );
        doorResponseDTO.description( door.getDescription() );
        if ( door.getPrice() != null ) {
            doorResponseDTO.price( BigDecimal.valueOf( door.getPrice() ) );
        }
        doorResponseDTO.status( door.getStatus() );
        doorResponseDTO.color( door.getColor() );
        List<String> list = door.getImages();
        if ( list != null ) {
            doorResponseDTO.images( new ArrayList<String>( list ) );
        }

        doorResponseDTO.category( mapCategory(door.getCategory()) );

        return doorResponseDTO.build();
    }
}
