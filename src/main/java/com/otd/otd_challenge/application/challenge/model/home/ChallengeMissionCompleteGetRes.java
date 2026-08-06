package com.otd.otd_challenge.application.challenge.model.home;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class ChallengeMissionCompleteGetRes {
    private long userId;
    private long cdId;
    private LocalDate successDate;

}
