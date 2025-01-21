package uz.pdp.repository;

/**
 * Custom repository interface for BasketItem operations.
 * 
 * Fun fact: This is where we handle the special door-to-door delivery of delete operations! 🚪
 */
public interface CustomBasketItemRepository {
    void deleteAllByBasketId(Long basketId);
    void deleteBasketItemById(Long id);
}
