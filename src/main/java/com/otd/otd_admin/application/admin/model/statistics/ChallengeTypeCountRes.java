package com.otd.otd_admin.application.admin.model.statistics;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChallengeTypeCountRes {
    private String type;
    private Long count;
}
