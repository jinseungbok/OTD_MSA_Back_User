package com.otd.otd_pointshop.application.point.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PointPutReq {
    private Long pointId;
    private Integer pointScore;
    private String pointItemName;
    private String pointItemContent;
    private List<String> images;
    private Long pointCategoryId;
}
