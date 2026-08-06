package com.otd.otd_admin.application.admin.model.statistics;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class AdminStatisticsChallengeDto {
    private List<TierCountRes> tierCount;
    private List<ChallengeSuccessRateCountRes> challengeSuccessRateCount;
    private List<ChallengeParticipationCountRes> challengeParticipationCount;
    private List<ChallengeTypeCountRes> challengeTypeCount;
}
