package uz.pdp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import uz.pdp.entity.Address;


@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findAllByOrderByCity();

    List<Address> findByCityContainingIgnoreCase(String city);
    
    List<Address> findByStreetContainingIgnoreCase(String street);
    
    @Query("SELECT a FROM Address a WHERE " +
           "LOWER(a.city) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.street) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Address> searchAddresses(@Param("search") String search);
    
    @Query(value = "SELECT * FROM addresses a " +
           "ORDER BY (POW(a.latitude - :lat, 2) + POW(a.longitude - :lng, 2)) ASC " +
           "LIMIT 1", nativeQuery = true)
    Optional<Address> findNearestAddress(
        @Param("lat") Double latitude, 
        @Param("lng") Double longitude
    );
    
    boolean existsByPhoneIgnoreCase(String phone);
}