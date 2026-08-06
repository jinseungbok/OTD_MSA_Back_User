package com.otd.otd_admin.application.admin.model;

import com.otd.configuration.enumcode.model.EnumChallengeRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminChallengeDto {
    private Long cdId;
    private String cdName;
    private String cdType;
    private int cdGoal;
    private String cdUnit;
    private int cdReward;
    private Integer xp;
    private EnumChallengeRole tier;
    private String cdImage;
}
