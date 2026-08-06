package com.otd.otd_admin.application.admin.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminChallengeProgress {
    private Long id;
    private String cdName;
    private String cdGoal;
    private String cdType;
    private String cdUnit;
    private String nickName;
    private String name;
    private Double totalRecord;
    private boolean isSuccess;

    // 포맷팅
    private String goal;
    private String record;
}
