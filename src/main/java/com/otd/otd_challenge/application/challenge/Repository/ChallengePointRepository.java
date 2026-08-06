package com.otd.otd_challenge.application.challenge.Repository;

import com.otd.otd_challenge.entity.ChallengePointHistory;
import com.otd.otd_user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChallengePointRepository extends JpaRepository<ChallengePointHistory, Long> {

  @Query("SELECT ch FROM ChallengePointHistory ch WHERE ch.user.userId = :userId")
  List<ChallengePointHistory> findByUserId(Long userId);

  void deleteAllByUser(User user);
}
