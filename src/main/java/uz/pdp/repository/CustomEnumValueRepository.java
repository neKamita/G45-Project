package uz.pdp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.entity.CustomEnumValue;
import java.util.List;

public interface CustomEnumValueRepository extends JpaRepository<CustomEnumValue, Long> {
    List<CustomEnumValue> findByEnumTypeAndIsActiveTrue(String enumType);
    boolean existsByNameAndEnumType(String name, String enumType);
}
