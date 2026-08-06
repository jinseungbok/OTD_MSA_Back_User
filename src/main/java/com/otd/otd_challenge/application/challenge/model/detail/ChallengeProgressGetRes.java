package com.otd.otd_challenge.application.challenge.model.detail;

import com.otd.otd_user.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ChallengeProgressGetRes {
    private int userId;
    private int cpId;
    private int cdId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalRecord;
    private String image;
    private String type;
    private String name;
    private int reward;
    private User user;
}
