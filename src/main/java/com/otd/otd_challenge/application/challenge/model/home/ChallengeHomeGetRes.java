package com.otd.otd_challenge.application.challenge.model.home;

import com.otd.configuration.enumcode.model.EnumChallengeRole;
import com.otd.otd_challenge.application.challenge.model.detail.ChallengeProgressGetRes;
import com.otd.otd_challenge.entity.ChallengeDefinition;
import com.otd.otd_user.entity.User;
import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeHomeGetRes {
    private UserInfoGetRes user;
    private int success;

    private List<ChallengeProgressGetRes> personalChallenge;
    private List<ChallengeProgressGetRes> weeklyChallenge;
    private List<ChallengeProgressGetRes> competitionChallenge;

    private List<ChallengeDefinition> dailyMission;
    private List<ChallengeMissionCompleteGetRes> missionComplete;

    private EnumChallengeRole role;
}
