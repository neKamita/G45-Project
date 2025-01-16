package uz.pdp.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import uz.pdp.entity.Door;
import uz.pdp.enums.Color;

import java.util.List;

@Repository
public interface DoorRepository extends JpaRepository<Door, Long> {

    List<Door> findBySellerId(Long userId);
    Page<Door> findAll(@NotNull Pageable pageable);

    @Query("SELECT d FROM Door d WHERE " +
           "d.material = :material AND " +
           "d.color = :color AND " +
           "d.price BETWEEN :minPrice AND :maxPrice AND " +
           "d.id != :doorId AND " +
           "d.active = true")
    List<Door> findByMaterialAndColorAndPriceBetweenAndIdNot(
        String material,
        Color color, 
        double minPrice,
        double maxPrice,
        Long doorId,
        Pageable pageable
    );
}