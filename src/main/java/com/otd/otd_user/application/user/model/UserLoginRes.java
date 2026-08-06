package com.otd.otd_user.application.user.model;

import com.otd.configuration.enumcode.model.EnumChallengeRole;
import com.otd.configuration.enumcode.model.EnumUserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class UserLoginRes {
    private Long userId;
    private String name;
    private String nickName;
    private String pic;
    private String email;
    private String gender;
    private Integer age;
    private int point;
    private int xp;
    private int onboardingCompleted;
    private List<String> roles;
    private LocalDateTime lastLoginAt;
    private EnumChallengeRole challengeRole;
    private EnumUserRole userRole;
}
