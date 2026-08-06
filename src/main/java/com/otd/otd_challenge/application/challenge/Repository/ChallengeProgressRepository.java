package com.otd.otd_challenge.application.challenge.Repository;

import com.otd.otd_challenge.entity.ChallengeProgress;
import jakarta.transaction.Transactional;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ChallengeProgressRepository extends JpaRepository<ChallengeProgress, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE ChallengeProgress SET isSuccess = true WHERE cpId = :cpId")
    int updateIsSuccess(@Param("cpId") Long cpId);

    @Query("SELECT cp FROM ChallengeProgress cp " +
            "WHERE cp.user.userId = :userId " +
            "AND cp.challengeDefinition.cdName = :name " +
            "AND :recordDate <= cp.endDate")
    Optional<ChallengeProgress> findActiveProgress(
            @Param("userId") Long userId,
            @Param("name") String name,
            @Param("recordDate") LocalDate recordDate);

    @Query("SELECT cp FROM ChallengeProgress cp " +
            "WHERE cp.user.userId = :userId " +
            "AND cp.challengeDefinition.cdType = 'personal' " +
            "AND cp.startDate <= :recordDate " +
            "AND cp.endDate >= :recordDate")
    List<ChallengeProgress> findActiveProgressByType(
            @Param("userId") Long userId,
            @Param("recordDate") LocalDate recordDate
    );


    @Query("SELECT cp FROM ChallengeProgress cp WHERE cp.user.userId = :userId")
    List<ChallengeProgress> findByUserId(Long userId);

    @Query("SELECT cp.challengeDefinition.cdName FROM ChallengeProgress cp " +
            "WHERE cp.user.userId = :userId " +
            "AND cp.startDate <= :recordDate " +
            "AND cp.endDate >= :recordDate")
    List<String> findActiveChallengeNames(@Param("userId") Long userId
                                        , @Param("recordDate") LocalDate recordDate);

}
