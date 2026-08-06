package com.otd.otd_pointshop.application.point.model;

import com.otd.otd_pointshop.entity.PointImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointGetRes {
    private Long pointId;
    private Integer pointScore;
    private String pointItemName;
    private String pointItemContent;

    // 이미지 응답 DTO
    private List<PointImageRes> images;
    private LocalDateTime createdAt;

    // 유저 현재 포인트
    private int userCurrentPoint;

    private String category;
}
