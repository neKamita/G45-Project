package uz.pdp.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of custom BasketItem repository operations.
 * 
 * Fun fact: This is where the magic happens! We're like door-removal experts,
 * but for digital shopping carts! 🛒✨
 */
@Repository
@Transactional
public class CustomBasketItemRepositoryImpl implements CustomBasketItemRepository {
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void deleteAllByBasketId(Long basketId) {
        entityManager.createNativeQuery("DELETE FROM basket_item WHERE basket_id = :basketId")
            .setParameter("basketId", basketId)
            .executeUpdate();
    }

    @Override
    public void deleteBasketItemById(Long id) {
        entityManager.createNativeQuery("DELETE FROM basket_item WHERE id = :id")
            .setParameter("id", id)
            .executeUpdate();
    }
}
