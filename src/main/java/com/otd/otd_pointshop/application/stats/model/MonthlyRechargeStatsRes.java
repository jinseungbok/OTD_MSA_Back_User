package com.otd.otd_pointshop.application.stats.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRechargeStatsRes {

    private String month;    // 예: "2025-09"
    private Long totalAmount; // 해당 월의 충전 합계 포인트
}
