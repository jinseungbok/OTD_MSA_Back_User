package com.otd.otd_admin.application.admin.model.dashboard;

import com.otd.otd_user.entity.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class AdminDashBoardPointDto {
    private int totalPoint;
//    private int usedPoint;
    private List<User> pointTop5User;
//    private Double avgUsedPoint;
}
