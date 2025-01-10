package uz.pdp.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import uz.pdp.entity.EmailVerification;
import uz.pdp.enums.VerificationType;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByUserIdAndTypeAndVerifiedFalseAndExpiryTimeAfter(
            Long userId, 
            VerificationType type, 
            LocalDateTime now
    );

    Optional<EmailVerification> findByUserIdAndVerificationCodeAndTypeAndVerifiedFalseAndExpiryTimeAfter(
            Long userId,
            String verificationCode,
            VerificationType type,
            LocalDateTime now
    );

    boolean existsByUserIdAndTypeAndVerifiedFalseAndExpiryTimeAfter(
            Long userId,
            VerificationType type,
            LocalDateTime now
    );

    @Modifying
    @Query("UPDATE EmailVerification e SET e.verified = true WHERE e.id = :id")
    void markAsVerified(@Param("id") Long id);
} 