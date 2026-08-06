package com.otd.otd_pointshop.application.purchase.model;

import com.otd.otd_pointshop.entity.Point;
import com.otd.otd_pointshop.entity.PurchaseHistory;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchasePostRes {
    private Long purchaseId; // 구매 ID
    private Long pointId; // 상품 ID
    private String pointItemName; // 상품 이름
    private Integer pointScore; // 사용 포인트
    private String pointItemImage; // 상품 이미지 URL
    private LocalDateTime purchaseAt; // 구매 일시

    public static PurchasePostRes fromEntity(PurchaseHistory entity) {
        if (entity == null) return null;

        Point point = entity.getPoint();
        String imageUrl = null;

        if (point != null && point.getPointItemImages() != null && !point.getPointItemImages().isEmpty()) {
            imageUrl = point.getPointItemImages().get(0).getImageUrl();
        }

        return PurchasePostRes.builder()
                .purchaseId(entity.getPurchaseId())
                .pointId(point != null ? point.getPointId() : null)
                .pointItemName(point != null ? point.getPointItemName() : "상품 정보 없음")
                .pointScore(point != null ? point.getPointScore() : 0)
                .pointItemImage(imageUrl)
                .purchaseAt(entity.getPurchaseAt())
                .build();
    }
}