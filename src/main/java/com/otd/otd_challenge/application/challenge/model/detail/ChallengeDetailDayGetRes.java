package com.otd.otd_challenge.application.challenge.model.detail;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@ToString
public class ChallengeDetailDayGetRes {
    private long userId;
    private long cpId;
    private long cdId;
    private String name;
    private String unit;
    private int goal;
    @JsonIgnore
    private Integer date;
    private List<Integer> recDate; // 쿼리로 day만 get
    private LocalDate startDate;
    private LocalDate endDate;
    private int reward;
    private boolean isSuccess;
}
