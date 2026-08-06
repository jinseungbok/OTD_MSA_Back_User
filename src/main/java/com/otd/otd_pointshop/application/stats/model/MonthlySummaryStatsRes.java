package com.otd.otd_pointshop.application.stats.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// 월별 요약 통계 DTO (충전/구매 합계 및 순 변화)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlySummaryStatsRes {

    private String month;          // 월 (YYYY-MM)
    private Long totalRecharge;    // 해당 월의 총 충전 금액
    private Long totalPurchase;    // 해당 월의 총 구매 금액
    private Long netChange;        // 순 변화 (충전 - 구매)
}
