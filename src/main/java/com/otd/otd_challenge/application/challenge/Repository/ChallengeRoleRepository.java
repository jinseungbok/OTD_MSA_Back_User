package com.otd.otd_challenge.application.challenge.Repository;

import com.otd.configuration.enumcode.model.EnumChallengeRole;
import com.otd.otd_user.entity.UserRole;
import com.otd.otd_user.entity.UserRoleIds;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ChallengeRoleRepository extends JpaRepository<UserRole, UserRoleIds> {

    @Modifying
    @Query("UPDATE UserRole ur SET ur.userRoleIds.challengeCode = :challengeCode WHERE ur.user.userId = :userId")
    void updateChallengeRole(Long userId, EnumChallengeRole challengeCode);
}
