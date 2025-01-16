package uz.pdp.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.entity.Door;

import java.util.List;

@Repository
public interface DoorRepository extends JpaRepository<Door, Long> {

    List<Door> findBySellerId(Long userId);
    Page<Door> findAll(@NotNull Pageable pageable);
}