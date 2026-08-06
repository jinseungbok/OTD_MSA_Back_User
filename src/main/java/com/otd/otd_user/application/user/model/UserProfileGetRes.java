package com.otd.otd_user.application.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileGetRes {
    private Long userId;
    private String pic;
    private LocalDateTime createdAt;
    private String uid;
    private String name;
    private String nickName;
    private String email;
    private String phone;
    private String birthDate;
    private String gender;
    private LocalDateTime lastLoginAt;
    private int point;

}