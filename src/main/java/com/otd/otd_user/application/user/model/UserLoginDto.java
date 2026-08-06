package com.otd.otd_user.application.user.model;

import com.otd.configuration.model.JwtUser;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserLoginDto {
    private UserLoginRes userLoginRes; //응답용
    private JwtUser jwtUser; //JWT 발행 때 사용
}
