package com.otd.otd_pointshop.application.purchase.model;

import com.otd.otd_pointshop.entity.PurchaseHistory;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseHistoryRes {

    private Long purchaseId;          // 구매 내역 ID
    private Long userId;              // 사용자 ID
    private String userName;          // 사용자 이름
    private Long pointId;             // 포인트 상품 ID
    private String pointItemName;     // 상품명
    private int pointScore;           // 상품 포인트 가격
    private String pointItemImage;    // 상품 이미지 URL
    private LocalDateTime purchaseAt; // 구매 시각
    private Integer userCurrentPoint; // 구매 후 유저 잔여 포인트

    // JPQL용 생성자 (Repository 쿼리에서 사용)
    public PurchaseHistoryRes(
            Long purchaseId,
            Long pointId,
            String pointItemName,
            int pointScore,
            String pointItemImage,
            LocalDateTime purchaseAt
    ) {
        this.purchaseId = purchaseId;
        this.pointId = pointId;
        this.pointItemName = pointItemName;
        this.pointScore = pointScore;
        this.pointItemImage = pointItemImage;
        this.purchaseAt = purchaseAt;
    }

    // 엔티티 → DTO 변환 (Service/Entity 기반)
    public static PurchaseHistoryRes fromEntity(PurchaseHistory entity) {
        if (entity == null) return null;

        Long userId = (entity.getUser() != null) ? entity.getUser().getUserId() : null;
        String userName = (entity.getUser() != null) ? entity.getUser().getName() : null;
        Integer currentPoint = (entity.getUser() != null) ? entity.getUser().getPoint() : null;

        Long pointId = (entity.getPoint() != null) ? entity.getPoint().getPointId() : null;
        String pointItemName = (entity.getPoint() != null) ? entity.getPoint().getPointItemName() : "상품 정보 없음";
        int pointScore = (entity.getPoint() != null) ? entity.getPoint().getPointScore() : 0;

        String imageUrl = null;
        if (entity.getPoint() != null &&
                entity.getPoint().getPointItemImages() != null &&
                !entity.getPoint().getPointItemImages().isEmpty()) {
            imageUrl = entity.getPoint().getPointItemImages().get(0).getImageUrl();
        }

        return PurchaseHistoryRes.builder()
                .purchaseId(entity.getPurchaseId())
                .userId(userId)
                .userName(userName)
                .pointId(pointId)
                .pointItemName(pointItemName)
                .pointScore(pointScore)
                .pointItemImage(imageUrl)
                .purchaseAt(entity.getPurchaseAt())
                .userCurrentPoint(currentPoint)
                .build();
    }
}
