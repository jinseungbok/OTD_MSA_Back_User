package com.otd.otd_user.application.user.model;

import com.otd.otd_user.entity.UserRole;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    @Query("SELECT ur FROM UserRole ur WHERE ur.user.userId = :userId")
    Optional<UserRole> findByUserId(@Param("userId") Long userId);}
