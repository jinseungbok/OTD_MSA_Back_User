package com.otd.otd_admin.application.admin.model.dashboard;

import com.otd.otd_challenge.entity.ChallengeDefinition;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class AdminDashBoardChallengeDto {
    private int totalChallengeCount;
    private List<ChallengeDefinition> participantTop5Challenge;
    private List<ChallengeDefinition> failTop3Challenge;
    private Double successRate;
}
