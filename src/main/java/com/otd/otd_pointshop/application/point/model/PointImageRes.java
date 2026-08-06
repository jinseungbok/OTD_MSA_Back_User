package com.otd.otd_pointshop.application.point.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PointImageRes {
    private Long imageId;
    private String imageUrl;
    private String imageType;
    private String altText;
}
