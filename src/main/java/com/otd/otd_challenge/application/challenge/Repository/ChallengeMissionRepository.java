package com.otd.otd_challenge.application.challenge.Repository;

import com.otd.otd_challenge.entity.ChallengeMission;
import com.otd.otd_user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeMissionRepository extends JpaRepository<ChallengeMission, Long> {
    void deleteAllByUser(User user);
}
