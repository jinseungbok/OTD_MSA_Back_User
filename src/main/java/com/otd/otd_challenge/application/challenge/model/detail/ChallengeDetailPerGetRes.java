package com.otd.otd_challenge.application.challenge.model.detail;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.otd.configuration.util.FormattedTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.text.DecimalFormat;
import java.util.List;

@Getter
@ToString
@Setter
public class ChallengeDetailPerGetRes {
    private long userId;
    private long cpId;
    private long cdId;
    private String name;
    private int reward;
    private double totalRecord;
    @JsonIgnore
    private double goal;
    private String formattedTotalRecord; // unit과 합체
    private String formattedGoal; // unit과 합체
    private Double percent;
    private int totalUsers;
    private int myRank;
    private String unit;
    private List<ChallengeRankGetRes> topRanking;
    private List<ChallengeRankGetRes> aroundRanking;
    private boolean isSuccess;

    public void setFormattedFields() {
        if ("분".equals(unit)) {
            this.formattedGoal = FormattedTime.formatMinutes(goal);
            this.formattedTotalRecord = FormattedTime.formatMinutes(totalRecord);
        } else {
            DecimalFormat df = (goal % 1 == 0)
                    ? new DecimalFormat("0")
                    : new DecimalFormat("0.0");
            this.formattedGoal = df.format(goal) + unit;

            DecimalFormat df2 = (totalRecord % 1 == 0)
                    ? new DecimalFormat("0")
                    : new DecimalFormat("0.0");
            this.formattedTotalRecord = df2.format(totalRecord) + unit;
        }
    }
}
