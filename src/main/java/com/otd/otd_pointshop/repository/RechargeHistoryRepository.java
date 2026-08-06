package com.otd.otd_pointshop.repository;

import com.otd.otd_pointshop.entity.RechargeHistory;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RechargeHistoryRepository extends JpaRepository<RechargeHistory, Long> {
    // 특정 유저 충전 이력 조회
    List<RechargeHistory> findByUser_UserId(Long userId);

    // 관리자 충전 내역
    List<RechargeHistory> findByAdminId(Long adminId);

    // 특정 유저 총 충전 금액
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM RechargeHistory r WHERE r.user.userId = :userId")
    Optional<Integer> findTotalRechargeByUserId(@Param("userId") Long userId);

    // 관리자 충전 월별 통계
    @Query(value = """
        SELECT 
            DATE_FORMAT(r.recharge_at, '%Y-%m') AS month,
            SUM(r.amount) AS totalAmount
        FROM recharge_history r
        WHERE r.admin_id = :adminId
        GROUP BY DATE_FORMAT(r.recharge_at, '%Y-%m')
        ORDER BY DATE_FORMAT(r.recharge_at, '%Y-%m') ASC
    """, nativeQuery = true)
    List<Object[]> findMonthlyRechargeStatsByAdmin(@Param("adminId") Long adminId);

    // (관리자, 사용자) 월별 총 충전 금액
    @Query(value = """
        SELECT 
            DATE_FORMAT(r.recharge_at, '%Y-%m') AS month,
            SUM(r.amount) AS totalAmount
        FROM recharge_history r
        GROUP BY DATE_FORMAT(r.recharge_at, '%Y-%m')
        ORDER BY DATE_FORMAT(r.recharge_at, '%Y-%m') ASC
    """, nativeQuery = true)
    List<Object[]> findMonthlyRechargeTotals();

    // (관리자, 사용자) 전체 충전 포인트 총합
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM RechargeHistory r")
    Integer findTotalRechargeAmount();
}
