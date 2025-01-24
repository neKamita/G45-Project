package uz.pdp.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import uz.pdp.dto.FurnitureDoorCreateDTO;
import uz.pdp.dto.FurnitureDoorResponseDTO;
import uz.pdp.entity.FurnitureDoor;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-01-24T21:57:58+0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.1 (Oracle Corporation)"
)
@Component
public class FurnitureDoorMapperImpl implements FurnitureDoorMapper {

    @Override
    public FurnitureDoor toEntity(FurnitureDoorCreateDTO dto) {
        if ( dto == null ) {
            return null;
        }

        FurnitureDoor furnitureDoor = new FurnitureDoor();

        furnitureDoor.setName( dto.getName() );
        furnitureDoor.setMaterial( dto.getMaterial() );
        furnitureDoor.setDescription( dto.getDescription() );
        furnitureDoor.setPrice( dto.getPrice() );
        furnitureDoor.setDimensions( dto.getDimensions() );
        furnitureDoor.setStockQuantity( dto.getStockQuantity() );
        furnitureDoor.setFurnitureType( dto.getFurnitureType() );

        initializeCollections( furnitureDoor );

        return furnitureDoor;
    }

    @Override
    public FurnitureDoorResponseDTO toDto(FurnitureDoor entity) {
        if ( entity == null ) {
            return null;
        }

        FurnitureDoorResponseDTO furnitureDoorResponseDTO = new FurnitureDoorResponseDTO();

        List<String> list = entity.getImages();
        if ( list != null ) {
            furnitureDoorResponseDTO.setImageUrls( new ArrayList<String>( list ) );
        }
        furnitureDoorResponseDTO.setId( entity.getId() );
        furnitureDoorResponseDTO.setName( entity.getName() );
        furnitureDoorResponseDTO.setMaterial( entity.getMaterial() );
        furnitureDoorResponseDTO.setDescription( entity.getDescription() );
        furnitureDoorResponseDTO.setPrice( entity.getPrice() );
        furnitureDoorResponseDTO.setDimensions( entity.getDimensions() );
        furnitureDoorResponseDTO.setStockQuantity( entity.getStockQuantity() );
        furnitureDoorResponseDTO.setFurnitureType( entity.getFurnitureType() );

        return furnitureDoorResponseDTO;
    }

    @Override
    public void updateEntityFromDto(FurnitureDoorCreateDTO dto, FurnitureDoor entity) {
        if ( dto == null ) {
            return;
        }

        entity.setName( dto.getName() );
        entity.setMaterial( dto.getMaterial() );
        entity.setDescription( dto.getDescription() );
        entity.setPrice( dto.getPrice() );
        entity.setDimensions( dto.getDimensions() );
        entity.setStockQuantity( dto.getStockQuantity() );
        entity.setFurnitureType( dto.getFurnitureType() );

        initializeCollections( entity );
    }
}
