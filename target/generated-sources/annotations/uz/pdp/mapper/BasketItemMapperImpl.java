package uz.pdp.mapper;

import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import uz.pdp.dto.BasketItemDTO;
import uz.pdp.entity.BasketItem;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-02-09T15:39:35+0500",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.41.0.z20250115-2156, environment: Java 21.0.5 (Eclipse Adoptium)"
)
@Component
public class BasketItemMapperImpl implements BasketItemMapper {

    @Override
    public BasketItemDTO toDto(BasketItem basketItem) {
        if ( basketItem == null ) {
            return null;
        }

        BasketItemDTO basketItemDTO = new BasketItemDTO();

        basketItemDTO.setImageUrl( basketItem.getImage() );
        basketItemDTO.setId( basketItem.getId() );
        basketItemDTO.setItemId( basketItem.getItemId() );
        basketItemDTO.setName( basketItem.getName() );
        basketItemDTO.setPrice( BigDecimal.valueOf( basketItem.getPrice() ) );
        basketItemDTO.setQuantity( basketItem.getQuantity() );
        basketItemDTO.setType( basketItem.getType() );

        basketItemDTO.setTotalPrice( calculateTotalPrice(basketItem) );

        return basketItemDTO;
    }

    @Override
    public BasketItem toEntity(BasketItemDTO dto) {
        if ( dto == null ) {
            return null;
        }

        BasketItem basketItem = new BasketItem();

        basketItem.setImage( dto.getImageUrl() );
        basketItem.setId( dto.getId() );
        basketItem.setItemId( dto.getItemId() );
        basketItem.setName( dto.getName() );
        if ( dto.getPrice() != null ) {
            basketItem.setPrice( dto.getPrice().doubleValue() );
        }
        basketItem.setQuantity( dto.getQuantity() );
        basketItem.setType( dto.getType() );

        return basketItem;
    }

    @Override
    public void updateEntityFromDto(BasketItemDTO dto, BasketItem entity) {
        if ( dto == null ) {
            return;
        }

        entity.setImage( dto.getImageUrl() );
        entity.setItemId( dto.getItemId() );
        entity.setName( dto.getName() );
        if ( dto.getPrice() != null ) {
            entity.setPrice( dto.getPrice().doubleValue() );
        }
        entity.setQuantity( dto.getQuantity() );
        entity.setType( dto.getType() );
    }
}
