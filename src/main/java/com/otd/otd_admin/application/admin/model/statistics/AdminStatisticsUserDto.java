package com.otd.otd_admin.application.admin.model.statistics;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class AdminStatisticsUserDto {
    private List<GenderCountRes> genderCount;
    private List<AgeCountRes> ageCount;
    private List<SignInCountRes> signInCount;
}
