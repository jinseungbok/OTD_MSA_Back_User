package com.otd.otd_admin.application.admin.model;

import com.otd.otd_challenge.entity.ChallengePointHistory;
import com.otd.otd_challenge.entity.ChallengeProgress;
import lombok.*;

import java.util.List;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserDetailGetRes {
  private List<ChallengeProgress> challengeProgress;
  private List<ChallengePointHistory> challengePointHistory;
  private List<AdminPurchaseDto> pointPurchases;
}
