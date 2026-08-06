package com.otd.otd_pointshop.application.point.model;

import lombok.Data;

@Data
public class PointGetReq {
    private Long userId;
    private Long pointId;
    private String pointItemName;
    private String pointItemContent;
}
