package com.otd.otd_challenge.application.challenge.model.settlement;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Setter
@Getter
@ToString
public class ChallengeSettlementGetReq {
  private Long userId;
  private LocalDate settlementDate;
  private String type;
}
