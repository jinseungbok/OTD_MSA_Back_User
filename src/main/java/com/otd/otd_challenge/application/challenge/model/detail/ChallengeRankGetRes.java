package com.otd.otd_challenge.application.challenge.model.detail;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ChallengeRankGetRes {
    private long userId;
    private String nickName;
    @JsonIgnore
    private String name;
    private String pic;
    private double totalRecord;
    private String formattedTotalRecord;
    private int rank;
}
