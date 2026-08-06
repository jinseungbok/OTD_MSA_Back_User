package com.otd.otd_admin.application.admin.model;

import com.otd.configuration.enumcode.model.EnumChallengeRole;
import com.otd.configuration.enumcode.model.EnumUserRole;
import com.otd.configuration.security.SignInProviderType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class AdminUserGetRes {
    private long userId;
    private String name;
    private String email;
    private String gender;
    private String phone;
    private int point;
    private String uid;
    private int xp;
    private LocalDateTime createdAt;
    private String birthDate;
    private String nickName;
    private SignInProviderType signInProviderType;
    private EnumUserRole userRole;
    private EnumChallengeRole challengeRole;


}
