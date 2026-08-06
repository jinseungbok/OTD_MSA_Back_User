package com.otd.otd_pointshop.repository;

import com.otd.otd_pointshop.application.purchase.model.PurchaseHistoryRes;
import com.otd.otd_pointshop.entity.Point;
import com.otd.otd_pointshop.entity.PurchaseHistory;
import com.otd.otd_user.entity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface PurchaseHistoryRepository extends JpaRepository<PurchaseHistory,Long> {
    // 특정 유저 전체 구매 이력
    List<PurchaseHistory> findByUser(User user);

    // 특정 유저 ID로 구매 이력
    List<PurchaseHistory> findByUser_UserId(Long userId);

    // 사용자별 총 사용 포인트
    @Query("SELECT COALESCE(SUM(p.point.pointScore), 0) " +
            "FROM PurchaseHistory p " +
            "WHERE p.user.userId = :userId")
    int findTotalSpentByUserId(@Param("userId") Long userId);

    // 사용자별 총 사용 포인트 상위 10명
    @Query("SELECT p.user.userId, p.user.name, SUM(p.point.pointScore) AS totalSpent " +
            "FROM PurchaseHistory p " +
            "GROUP BY p.user.userId, p.user.name " +
            "ORDER BY totalSpent DESC")
    List<Object[]> findTopUsersByTotalSpentPoints();

    // 월별 포인트 사용량
    @Query(value = """
        SELECT DATE_FORMAT(p.purchase_at, '%Y-%m') AS month, 
               SUM(pt.point_score) AS total_spent
        FROM purchase_history p
        JOIN point pt ON p.point_id = pt.point_id
        GROUP BY DATE_FORMAT(p.purchase_at, '%Y-%m')
        ORDER BY month ASC
    """, nativeQuery = true)
    List<Object[]> findMonthlySpentPoints();

    // 인기 상품 상위 TOP 10
    @Query("SELECT p.point.pointItemName, COUNT(p.point.pointId) AS purchaseCount " +
            "FROM PurchaseHistory p " +
            "GROUP BY p.point.pointItemName " +
            "ORDER BY purchaseCount DESC")
    List<Object[]> findTopPurchasedItems();

    List<PurchaseHistory> findByPoint(Point point);
    List<PurchaseHistory> findByPurchaseAt(LocalDateTime purchaseAt);
    List<PurchaseHistory> findByPurchaseAtBetween(LocalDateTime start, LocalDateTime end);

    // 기본 조회
    List<PurchaseHistory> findByUser_UserIdOrderByPurchaseAtDesc(Long userId);

    // 이미지까지 포함한 JOIN 조회 (사용자 구매 이력)
    @Query("""
        SELECT new com.otd.otd_pointshop.application.purchase.model.PurchaseHistoryRes(
            ph.purchaseId,
            p.pointId,
            p.pointItemName,
            p.pointScore,
            i.imageUrl,
            ph.purchaseAt
        )
        FROM PurchaseHistory ph
        JOIN ph.point p
        LEFT JOIN PointImage i ON p.pointId = i.point.pointId
        WHERE ph.user.userId = :userId
        ORDER BY ph.purchaseAt DESC
    """)
    List<PurchaseHistoryRes> findPurchaseHistoryWithImageByUserId(@Param("userId") Long userId);
}
