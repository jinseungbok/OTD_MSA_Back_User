package com.otd.otd_challenge.application.challenge.Repository;

import com.otd.otd_challenge.entity.ChallengeSettlementLog;
import com.otd.otd_user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeSettlementRepository extends JpaRepository<ChallengeSettlementLog, Long> {
    void deleteAllByUser(User user);
}
