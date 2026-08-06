package com.otd.otd_pointshop.application.purchase.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RechargePostReq {
    private Long adminId;   // 관리자 ID
    private Integer amount; // 충전 금액 (1 이상)
}
