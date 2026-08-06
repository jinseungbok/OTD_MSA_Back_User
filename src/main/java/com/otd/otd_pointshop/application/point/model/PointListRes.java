package com.otd.otd_pointshop.application.point.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PointListRes {
    private Long pointId;
    private String pointItemName;
    private String pointItemContent;
    private Integer pointScore;
    private LocalDateTime createdAt;
    private List<String> pointItemImage;
    private int userCurrentPoint;
}
