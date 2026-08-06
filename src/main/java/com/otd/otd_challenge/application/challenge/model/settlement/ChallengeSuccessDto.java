package com.otd.otd_challenge.application.challenge.model.settlement;

import lombok.*;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class ChallengeSuccessDto {
  private Long userId;
  private Long cdId;
  private Long cpId;
  private String name;
  private String type;
  private String image;
  private double totalRecord;
  private int reward;
  private int xp;
  private int rank;
}
