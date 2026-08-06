package com.otd.otd_challenge.application.challenge.model.home;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MainHomeGetRes {
    private int cdId;
    private String formatedName;
    private double percent;

    // 포멧팅과 퍼센테이지를 위한 get
    @JsonIgnore
    private int goal;
    @JsonIgnore
    private double totalRecord;
    @JsonIgnore
    private String name;
    @JsonIgnore
    private String unit;
    @JsonIgnore
    private String type;
}
