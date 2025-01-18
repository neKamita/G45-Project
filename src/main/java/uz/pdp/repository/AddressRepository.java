package uz.pdp.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import uz.pdp.entity.Address;

/**
 * Repository for address operations
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    /**
     * Finds all addresses ordered by city
     * 
     * @return List of addresses
     */
    List<Address> findAllByOrderByCity();

    /**
     * Finds addresses by city containing the given string (case-insensitive)
     * 
     * @param city City to search for
     * @return List of addresses
     */
    List<Address> findByCityContainingIgnoreCase(String city);

    /**
     * Finds an address by its ID
     * 
     * @param id Address ID to find
     * @return Optional containing the address if found
     */
    @Override
    Optional<Address> findById(Long id);

    /**
     * Finds addresses by user ID
     * 
     * @param userId User ID to search for
     * @return List of addresses
     */
    List<Address> findByUserId(Long userId);

    /**
     * Finds addresses by city containing the given string and user ID
     * 
     * @param city City to search for
     * @param userId User ID to search for
     * @return List of addresses
     */
    List<Address> findByCityContainingIgnoreCaseAndUserId(String city, Long userId);

    /**
     * Finds address by ID and user ID
     * 
     * @param id Address ID
     * @param userId User ID
     * @return Optional of Address
     */
    Optional<Address> findByIdAndUserId(Long id, Long userId);

    /**
     * Counts addresses by user ID
     * 
     * @param userId User ID to count addresses for
     * @return Number of addresses
     */
    long countByUserId(Long userId);

    /**
     * Finds the default address for a user
     * 
     * @param userId User ID to search for
     * @return Optional of Address
     */
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);

    /**
     * Checks if a user has any addresses
     * 
     * @param userId User ID to check
     * @return true if user has addresses
     */
    boolean existsByUserId(Long userId);

    /**
     * Finds addresses by street containing the given string (case-insensitive)
     * 
     * @param street Street to search for
     * @return List of addresses
     */
    List<Address> findByStreetContainingIgnoreCase(String street);

    /**
     * Finds addresses by name containing the given string (case-insensitive)
     * 
     * @param name Name to search for
     * @return List of addresses
     */
    List<Address> findByNameContainingIgnoreCase(String name);

    /**
     * Finds addresses by email
     * 
     * @param email Email to search for
     * @return Optional of Address
     */
    Optional<Address> findByEmail(String email);

    /**
     * Finds addresses by phone number
     * 
     * @param phoneNumber Phone number to search for
     * @return Optional of Address
     */
    Optional<Address> findByPhoneNumber(String phoneNumber);

    /**
     * Checks if an address exists with the given phone number (case-insensitive)
     * 
     * @param phoneNumber Phone number to check
     * @return true if exists, false otherwise
     */
    boolean existsByPhoneNumberIgnoreCase(String phoneNumber);

    /**
     * Custom query to find nearest addresses based on coordinates
     * 
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @return List of addresses ordered by distance
     */
    @Query(value = "SELECT a.* FROM addresses a " +
           "JOIN locations l ON a.location_id = l.id " +
           "ORDER BY (POW(:latitude - l.latitude, 2) + POW(:longitude - l.longitude, 2)) ASC " +
           "LIMIT 10", nativeQuery = true)
    List<Address> findNearestAddresses(@Param("latitude") Double latitude, 
                                     @Param("longitude") Double longitude);

    /**
     * Finds all addresses for a user
     * 
     * @param id User ID to find addresses for
     * @return Collection of addresses
     */
    Collection<Object> findAllByUserId(Long id);

    List<Address> findByCity(String city);
}