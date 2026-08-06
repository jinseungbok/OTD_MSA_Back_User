package com.otd.otd_pointshop.application.purchase.model;

import com.otd.otd_pointshop.entity.RechargeHistory;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechargeGetRes {
    private Long rechargeId;     // 충전 이력 ID
    private Long adminId;        // 관리자 ID
    private Long userId;         // 유저 ID
    private String name;         // 유저 이름
    private Integer amount;      // 충전 금액
    private LocalDateTime rechargeAt; // 충전 시각

    // DTO 변환
    public static RechargeGetRes fromEntity(RechargeHistory entity) {
        return RechargeGetRes.builder()
                .rechargeId(entity.getRechargeId())
                .adminId(entity.getAdminId())
                .userId(entity.getUser().getUserId())
                .name(entity.getUser().getName())
                .amount(entity.getAmount())
                .rechargeAt(entity.getRechargeAt())
                .build();
    }
}
