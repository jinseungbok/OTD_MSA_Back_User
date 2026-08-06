package com.otd.otd_admin.application.admin.Repository;

import com.otd.otd_challenge.entity.ChallengePointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminPointRepository extends JpaRepository<ChallengePointHistory,Integer> {
}
