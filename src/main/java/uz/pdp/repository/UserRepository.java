package uz.pdp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.pdp.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String name);
    Optional<User> findByEmail(String email);
    boolean existsByName(String name);
    boolean existsByEmail(String email);
}