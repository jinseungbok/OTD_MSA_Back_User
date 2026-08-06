package com.otd.otd_pointshop.application.point.model;

import lombok.Data;

import java.util.List;

@Data
public class PointPostReq {
    private Integer pointScore;
    private String pointItemName;
    private String pointItemContent;
    private List<String> images;
    private Long pointCategoryId;
}
