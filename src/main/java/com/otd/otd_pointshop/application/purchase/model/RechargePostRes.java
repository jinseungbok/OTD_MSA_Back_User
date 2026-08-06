package com.otd.otd_pointshop.application.purchase.model;

import com.otd.otd_pointshop.entity.RechargeHistory;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechargePostRes {
    private Long rechargeId;     // 충전 이력 ID
    private Long adminId;        // 관리자 ID
    private Long userId;         // 유저 ID
    private String name;         // 유저 이름
    private Integer amount;      // 충전 금액
    private Integer balance;     // 충전 후 잔액
    private LocalDateTime rechargeAt; // 충전 시각

    // DTO 변환
    public static RechargePostRes fromEntity(RechargeHistory entity) {
        return RechargePostRes.builder()
                .rechargeId(entity.getRechargeId())
                .adminId(entity.getAdminId())
                .userId(entity.getUser().getUserId())
                .name(entity.getUser().getName())
                .amount(entity.getAmount())
                .balance(entity.getUser().getPoint()) // 현재 유저 잔액 반영
                .rechargeAt(entity.getRechargeAt())
                .build();
    }
}