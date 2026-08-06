package com.otd.otd_pointshop.application.purchase.model;

import lombok.Data;

@Data
public class PurchaseBalanceRechargeReq {
    private Long userId;
    private Integer chargeAmount;
}
