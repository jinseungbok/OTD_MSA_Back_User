package com.otd.otd_user.application.term;

import com.otd.configuration.enumcode.model.EnumTermsType;
import com.otd.otd_user.entity.UserAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserAgreementRepository extends JpaRepository<UserAgreement, Long> {

    @Query("SELECT ua FROM UserAgreement ua " +
            "JOIN FETCH ua.terms t " +
            "WHERE ua.user.userId = :userId AND t.type = :termsType AND t.isActive = true")
    Optional<UserAgreement> findActiveAgreementByUserAndType(
            @Param("userId") Long userId,
            @Param("termsType") EnumTermsType enumTermsType
    );

    List<UserAgreement> findByUserUserIdOrderByAgreedAtDesc(Long userId);

    @Query("SELECT ua FROM UserAgreement ua " +
            "JOIN FETCH ua.terms t " +
            "WHERE ua.user.userId = :userId AND t.isActive = true")
    List<UserAgreement> findActiveAgreementsByUserId(@Param("userId") Long userId);
}