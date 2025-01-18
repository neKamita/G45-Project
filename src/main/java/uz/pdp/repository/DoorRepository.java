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

/**
 * Repository interface for Door entities.
 * The sacred place where doors are stored in the digital realm.
 * 
 * WARNING: This is where the door data lives. 
 * Don't fuck with the queries unless you REALLY know what you're doing.
 * Many developers have gone mad trying to optimize these queries...
 */
@Repository
public interface DoorRepository extends JpaRepository<Door, Long> {

    // Find all doors by their creator (because sellers are proud parents)
    List<Door> findBySellerId(Long sellerId);
    
    // Get all doors, but not all at once because we're not savages
    Page<Door> findAll(@NotNull Pageable pageable);

    /**
     * The legendary query that finds similar doors.
     * Some say it was written by the ancient ones...
     * 
     * @param material What the door is made of (hopefully not cardboard)
     * @param color The door's fashion statement
     * @param minPrice Minimum price (we're not running a charity here)
     * @param maxPrice Maximum price (we're not running a luxury boutique either)
     * @param doorId The ID of the door we're comparing to
     * @param pageable Because getting ALL doors at once would make the database cry
     * @return A list of doors that share the same vibes
     */
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

    /**
     * Search for doors by name or description.
     * AKA the "I know it exists somewhere!" function
     */
    List<Door> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String searchTerm, String searchTerm1);
}