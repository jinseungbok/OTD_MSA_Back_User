package com.otd.otd_pointshop.application.stats.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// 월별 상세 내역 DTO (충전 및 구매 내역)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyDetailRes {

    private String type;        // RECHARGE or PURCHASE
    private String date;        // 거래 일자 (YYYY-MM-DD)
    private String nickname;    // 사용자 닉네임
    private String itemName;    // 구매 상품명 (충전은 null)
    private Long amount;        // 거래 금액 (충전 금액 또는 포인트 사용량)
    private String createdAt;   // 기록 생성 시각 (보통 date와 동일)
}
