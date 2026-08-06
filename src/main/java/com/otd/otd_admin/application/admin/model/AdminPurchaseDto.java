package com.otd.otd_admin.application.admin.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class AdminPurchaseDto {
    private Long purchaseId;
    private LocalDateTime purchaseAt;

    private Long userId;
    private String nickName;
    private String name;
    private String email;

    private String itemContent;
    private int pointScore;
}
