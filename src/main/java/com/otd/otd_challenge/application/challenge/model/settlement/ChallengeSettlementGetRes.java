package com.otd.otd_challenge.application.challenge.model.settlement;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class ChallengeSettlementGetRes {
  private Long csId;
  private int totalPoint;
  private String image;
  private String name;
  private int reward;
  private int xp;
  private int extraReward;
}
