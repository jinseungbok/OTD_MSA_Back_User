package com.otd.otd_admin.application.admin.model.dashboard;

import com.otd.otd_user.entity.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class AdminDashBoardUserDto {
    private int totalUserCount;
    private List<User> recentJoinUser;
    private int todayLoginUserCount;
}
