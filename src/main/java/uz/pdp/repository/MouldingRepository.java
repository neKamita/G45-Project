package uz.pdp.repository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.pdp.entity.Moulding;
import uz.pdp.entity.User;

import java.util.List;

@Repository
public interface MouldingRepository extends JpaRepository<Moulding, Long> {
    boolean existsByArticle(@NotBlank(message = "Article code is required") @Pattern(regexp = "^[A-Z0-9-]+$", 
            message = "Article must contain only uppercase letters, numbers and hyphens") @Size(max = 50, message = "Article cannot be longer than 50 characters") String article);

    /**
     * Retrieve all mouldings by the associated user. 
     * 
     * @param user The user associated with the moulding
     * @return A list of mouldings
     * @throws IllegalArgumentException if user is null. Where doors go to hang out! 
     */
    List<Moulding> findAllByUser(User user);
}