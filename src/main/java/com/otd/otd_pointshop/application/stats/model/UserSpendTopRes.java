package com.otd.otd_pointshop.application.stats.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// 유저별 TOP 랭킹 DTO (충전 또는 구매 상위)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSpendTopRes {

    private Long userId;       // 사용자 ID
    private String nickname;   // 닉네임 (선택적으로 null일 수 있음)
    private Long totalAmount;  // 총 사용/충전 포인트 합계
    private String type;       // "RECHARGE" or "PURCHASE"
}
