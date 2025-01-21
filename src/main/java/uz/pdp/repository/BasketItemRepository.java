package uz.pdp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.pdp.entity.BasketItem;
import java.util.List;

/**
 * Repository for managing BasketItem entities.
 * 
 * Fun fact: This repository is like a digital storage room for your basket items! 🛒
 * No items get lost here - unless you tell us to delete them! 
 */
@Repository
public interface BasketItemRepository extends JpaRepository<BasketItem, Long>, CustomBasketItemRepository {
    List<BasketItem> findByBasketId(Long basketId);
}
