package com.otd.otd_challenge.application.challenge.Repository;

import com.otd.otd_challenge.entity.ChallengeProgress;
import com.otd.otd_challenge.entity.ChallengeRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ChallengeRecordRepository extends JpaRepository<ChallengeRecord, Integer> {

    boolean existsByChallengeProgressAndRecDate(ChallengeProgress cp, LocalDate recDate);

    ChallengeRecord findByChallengeProgressAndRecordId(ChallengeProgress cp, Long recordId);

    ChallengeRecord findByChallengeProgressAndRecDate(ChallengeProgress cp, LocalDate recDate);
}
