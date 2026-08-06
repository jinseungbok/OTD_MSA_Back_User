package com.otd.otd_challenge.application.challenge.model.home;

import com.otd.configuration.enumcode.model.EnumChallengeRole;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserInfoGetRes {
    private Long userId;
    private String name;
    private String nickName;
    private String pic;
    private int point;
    private int xp;
    private EnumChallengeRole challengeRole;
}
