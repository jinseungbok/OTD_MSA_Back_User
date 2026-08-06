package com.otd.otd_pointshop.application.stats.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// 인기 아이템 랭킹 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PopularItemRes {

    private String pointItemName;  // 상품명
    private Integer purchaseCount; // 구매 횟수
}
