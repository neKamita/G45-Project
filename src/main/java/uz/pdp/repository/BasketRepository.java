package uz.pdp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.entity.Basket;
import uz.pdp.entity.User;

import java.util.Optional;

/**
 * Repository for managing Basket entities.
 * 
 * Fun fact: This repository is like a digital shopping mall,
 * but with 100% less "you-must-buy-something" pressure! 🛍️
 */
@Repository
public interface BasketRepository extends JpaRepository<Basket, Long> {
    Optional<Basket> findByUser(User user);
    Optional<Basket> findByUserId(Long userId);
}
