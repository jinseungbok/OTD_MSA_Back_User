package com.otd.otd_pointshop.application.point.model;

import com.otd.configuration.model.JwtUser;
import lombok.Data;

@Data
public class PointDto {
    private PointGetRes pointGetRes;
    private JwtUser jwtUser;
}
