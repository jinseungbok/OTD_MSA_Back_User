package com.otd.otd_user.application.user.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointHistoryDTO {
    private Long chId;
    private String reason;
    private int point;
    private LocalDateTime createdAt;
}