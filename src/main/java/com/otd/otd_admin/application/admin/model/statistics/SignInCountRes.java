package com.otd.otd_admin.application.admin.model.statistics;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignInCountRes {
  private String month;
  private Long signInCount;
}
