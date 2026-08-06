package com.otd.otd_user.application.email;

import com.otd.otd_user.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    // 이메일과 타입으로 가장 최근 인증 정보 조회
    Optional<EmailVerification> findTopByEmailAndTypeOrderByCreatedAtDesc(
            String email, EmailVerification.VerificationType type);

    // 이메일과 타입으로 인증된 정보 조회 (만료되지 않은 것만)
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.email = :email AND ev.type = :type " +
            "AND ev.verified = true AND ev.expiresAt > :now")
    Optional<EmailVerification> findVerifiedByEmailAndType(
            @Param("email") String email,
            @Param("type") EmailVerification.VerificationType type,
            @Param("now") LocalDateTime now);

    // 만료된 인증 정보 삭제
    @Modifying
    @Query("DELETE FROM EmailVerification ev WHERE ev.expiresAt < :now")
    int deleteExpiredVerifications(@Param("now") LocalDateTime now);

    // 특정 이메일의 특정 타입 인증 정보 삭제
    @Modifying
    @Query("DELETE FROM EmailVerification ev WHERE ev.email = :email AND ev.type = :type")
    int deleteByEmailAndType(@Param("email") String email, @Param("type") EmailVerification.VerificationType type);
}