package com.otd.otd_user.application.user;

import com.otd.otd_challenge.entity.ChallengePointHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserChallengePointRepository extends JpaRepository<ChallengePointHistory, Long> {

    // 사용자별 포인트 내역 조회 (최신순)
    List<ChallengePointHistory> findByUserUserIdOrderByCreatedAtDesc(Long userId);
}