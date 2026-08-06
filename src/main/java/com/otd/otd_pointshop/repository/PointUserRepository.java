package com.otd.otd_pointshop.repository;

import com.otd.otd_pointshop.entity.PointUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointUserRepository extends JpaRepository<PointUser, Long> {
    // 특정 유저의 포인트 변동 내역 조회 (최신순)
    List<PointUser> findByUser_UserIdOrderByCreatedAtDesc(Long userId);
    List<PointUser> findByUser_UserId(Long userId);
}
