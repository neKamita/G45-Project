package uz.pdp.mapper;

import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import uz.pdp.dto.BasketItemDTO;
import uz.pdp.entity.BasketItem;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-01-24T22:07:28+0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.1 (Oracle Corporation)"
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
        basketItemDTO.setType( basketItem.getType() );
        basketItemDTO.setQuantity( basketItem.getQuantity() );
        basketItemDTO.setPrice( BigDecimal.valueOf( basketItem.getPrice() ) );
        basketItemDTO.setName( basketItem.getName() );

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
        basketItem.setType( dto.getType() );
        basketItem.setQuantity( dto.getQuantity() );
        if ( dto.getPrice() != null ) {
            basketItem.setPrice( dto.getPrice().doubleValue() );
        }
        basketItem.setName( dto.getName() );

        return basketItem;
    }

    @Override
    public void updateEntityFromDto(BasketItemDTO dto, BasketItem entity) {
        if ( dto == null ) {
            return;
        }

        entity.setImage( dto.getImageUrl() );
        entity.setItemId( dto.getItemId() );
        entity.setType( dto.getType() );
        entity.setQuantity( dto.getQuantity() );
        if ( dto.getPrice() != null ) {
            entity.setPrice( dto.getPrice().doubleValue() );
        }
        entity.setName( dto.getName() );
    }
}
