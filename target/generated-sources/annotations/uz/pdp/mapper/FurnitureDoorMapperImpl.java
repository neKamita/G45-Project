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
    date = "2025-02-09T16:58:31+0500",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.41.0.z20250115-2156, environment: Java 21.0.5 (Eclipse Adoptium)"
)
@Component
public class FurnitureDoorMapperImpl implements FurnitureDoorMapper {

    @Override
    public FurnitureDoor toEntity(FurnitureDoorCreateDTO dto) {
        if ( dto == null ) {
            return null;
        }

        FurnitureDoor furnitureDoor = new FurnitureDoor();

        furnitureDoor.setDescription( dto.getDescription() );
        furnitureDoor.setDimensions( dto.getDimensions() );
        furnitureDoor.setFurnitureType( dto.getFurnitureType() );
        furnitureDoor.setMaterial( dto.getMaterial() );
        furnitureDoor.setName( dto.getName() );
        furnitureDoor.setPrice( dto.getPrice() );
        furnitureDoor.setStockQuantity( dto.getStockQuantity() );

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
        furnitureDoorResponseDTO.setDescription( entity.getDescription() );
        furnitureDoorResponseDTO.setDimensions( entity.getDimensions() );
        furnitureDoorResponseDTO.setFurnitureType( entity.getFurnitureType() );
        furnitureDoorResponseDTO.setId( entity.getId() );
        furnitureDoorResponseDTO.setMaterial( entity.getMaterial() );
        furnitureDoorResponseDTO.setName( entity.getName() );
        furnitureDoorResponseDTO.setPrice( entity.getPrice() );
        furnitureDoorResponseDTO.setStockQuantity( entity.getStockQuantity() );

        return furnitureDoorResponseDTO;
    }

    @Override
    public void updateEntityFromDto(FurnitureDoorCreateDTO dto, FurnitureDoor entity) {
        if ( dto == null ) {
            return;
        }

        entity.setDescription( dto.getDescription() );
        entity.setDimensions( dto.getDimensions() );
        entity.setFurnitureType( dto.getFurnitureType() );
        entity.setMaterial( dto.getMaterial() );
        entity.setName( dto.getName() );
        entity.setPrice( dto.getPrice() );
        entity.setStockQuantity( dto.getStockQuantity() );

        initializeCollections( entity );
    }
}
