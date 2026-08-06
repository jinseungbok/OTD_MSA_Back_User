package com.otd.otd_challenge.application.challenge.model.settlement;

import lombok.*;

import java.time.LocalDate;

@Getter
@Builder(toBuilder = true)
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ChallengeSettlementDto {
  private LocalDate startDate;
  private LocalDate endDate;
  private String type;
  private Long userId;
}
