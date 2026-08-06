package com.otd.otd_pointshop.repository;

import com.otd.otd_pointshop.entity.Point;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PointRepository extends JpaRepository<Point,Long> {
    Optional<Point> findById(Long pointId);

    // 유저별 포인트 아이템 목록 (페이징)
    Page<Point> findByUser_UserId(Long userId, Pageable pageable);

    // 키워드 기반 검색
    Page<Point> findByUser_UserIdAndPointItemContentContaining(Long userId, String keyword, Pageable pageable);

    // 특정 유저의 포인트 내역 전체 조회
    List<Point> findByUser_UserId(Long userId);

    // 특정 유저의 총 포인트
    @Query("SELECT SUM(p.pointScore) FROM Point p WHERE p.user.userId = :userId")
    Integer findTotalPointsByUserId(@Param("userId") Long userId);

    List<Point> findByPointItemContentContaining(String keyword, Pageable pageable);
}
