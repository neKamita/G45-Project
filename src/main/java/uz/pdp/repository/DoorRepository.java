package uz.pdp.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.pdp.entity.Door;
import uz.pdp.enums.Color;
import uz.pdp.enums.Size;

import java.util.List;

/**
 * Repository interface for Door entities.
 * The sacred place where doors are stored in the digital realm.
 * 
 * WARNING: This is where the door data lives. 
 * Don't mess with the queries unless you REALLY know what you're doing.
 * Many developers have gone mad trying to optimize these queries...
 */
@Repository
public interface DoorRepository extends JpaRepository<Door, Long> {
    
    // Find all doors by their creator (because sellers are proud parents)
    Page<Door> findBySellerId(Long sellerId, Pageable pageable);
    
    // Get all doors, but not all at once because we're not savages
    @NotNull Page<Door> findAll(@NotNull Pageable pageable);
    
    /**
     * The ultimate door finder! 
     * This query is like a dating app for doors - matching your perfect criteria!
     */
    @Query("""
            SELECT DISTINCT d FROM Door d
            WHERE (:material IS NULL OR d.material = :material)
            AND (:minPrice IS NULL OR d.price >= :minPrice)
            AND (:maxPrice IS NULL OR d.price <= :maxPrice)
            AND (:color IS NULL OR d.color = :color)
            AND (:size IS NULL OR d.size = :size)
            AND (:manufacturer IS NULL OR d.manufacturer = :manufacturer)
            AND (:minWarranty IS NULL OR d.warrantyYears >= :minWarranty)
            AND (:customWidth IS NULL OR 
                (d.size = uz.pdp.enums.Size.CUSTOM AND d.customWidth = :customWidth))
            AND (:customHeight IS NULL OR 
                (d.size = uz.pdp.enums.Size.CUSTOM AND d.customHeight = :customHeight))
            AND (:searchTerm IS NULL OR 
                (LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR 
                 LOWER(d.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))))
            AND d.active = true
            """)
    Page<Door> searchDoors(
            @Param("material") String material,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("color") Color color,
            @Param("size") Size size,
            @Param("manufacturer") String manufacturer,
            @Param("minWarranty") Integer minWarranty,
            @Param("customWidth") Double customWidth,
            @Param("customHeight") Double customHeight,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

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
    @Query("""
            SELECT d FROM Door d
            WHERE d.material = :material
            AND d.color = :color
            AND d.price BETWEEN :minPrice AND :maxPrice
            AND d.id != :doorId
            AND d.active = true
            """)
    Page<Door> findSimilarDoors(
            @Param("material") String material,
            @Param("color") Color color,
            @Param("minPrice") double minPrice,
            @Param("maxPrice") double maxPrice,
            @Param("doorId") Long doorId,
            Pageable pageable
    );

    // Quick search by name or description
    Page<Door> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndActiveTrue(
            String searchTerm,
            String searchTerm1,
            Pageable pageable
    );

    List<Door> findByMaterialAndColorAndPriceBetweenAndIdNot(String material, Color color, double v, double v1, Long id, PageRequest of);

    List<Door> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String searchTerm, String searchTerm1);
}