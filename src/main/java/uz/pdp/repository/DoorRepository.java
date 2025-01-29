package uz.pdp.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uz.pdp.entity.Category;
import uz.pdp.entity.Door;
import uz.pdp.entity.User;
import uz.pdp.enums.Color;
import uz.pdp.enums.Size;

import java.util.List;
import java.util.Optional;

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
     * Now with super-optimized performance! 
     */
    @Query(value = """
            SELECT DISTINCT d FROM Door d
            WHERE d.active = true
            AND (:material IS NULL OR d.material = :material)
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
            ORDER BY 
                CASE 
                    WHEN :searchTerm IS NOT NULL AND LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) THEN 0
                    WHEN :searchTerm IS NOT NULL AND LOWER(d.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) THEN 1
                    ELSE 2 
                END,
                d.price ASC
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
     * The legendary query that finds similar doors - now with TURBO mode! 🏎️
     * Some say it was written by the ancient ones... and optimized by the modern ones!
     */
    @Query("""
            SELECT d FROM Door d
            WHERE d.active = true
            AND d.material = :material
            AND d.color = :color
            AND d.price BETWEEN :minPrice AND :maxPrice
            AND d.id != :doorId
            ORDER BY ABS(d.price - (
                SELECT d2.price FROM Door d2 WHERE d2.id = :doorId
            )) ASC
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

    List<Door> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String searchTerm, String searchTerm1);

    /**
     * Find all doors of a specific color that are active.
     * Because every color deserves its moment to shine! 🌈
     */
    List<Door> findByColorAndActiveTrue(Color color);

    /**
     * Find all variants of a door model with different colors.
     * It's like finding all the siblings in a door family! 🚪👨‍👩‍👧‍👦
     */
    List<Door> findByNameAndManufacturerAndSizeAndMaterialAndActiveTrue(
        String name,
        String manufacturer,
        Size size,
        String material
    );

    /**
     * Find all color variants of an exact door model from the same seller.
     * Like a door's identical siblings - same everything, just different colors! 🚪🎨
     */
    List<Door> findByNameAndManufacturerAndSizeAndMaterialAndSellerAndCustomWidthAndCustomHeightAndActiveTrueAndIdNot(
        String name,
        String manufacturer,
        Size size,
        String material,
        User seller,
        Double customWidth,
        Double customHeight,
        Long excludeDoorId
    );

    /**
     * Find all variants of a door model by base model ID.
     * Returns both the base model and all its color variants.
     */
    List<Door> findByBaseModelIdOrId(Long baseModelId, Long id);

    /**
     * Find a specific color variant of a door model.
     */
    Optional<Door> findByBaseModelIdAndColor(Long baseModelId, Color color);

    List<Door> findByMaterialAndColorAndPriceBetweenAndIdNot(String material, Color color, double v, double v1, Long id, PageRequest of);

    /**
     * Find all doors in a specific category.
     * The doors are like a big family, and categories are their homes! 🏠
     *
     * @param category The category to search in
     * @return List of doors in the category
     */
    List<Door> findByCategory(Category category);
}