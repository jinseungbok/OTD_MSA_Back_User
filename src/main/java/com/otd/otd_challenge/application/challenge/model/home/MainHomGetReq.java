package com.otd.otd_challenge.application.challenge.model.home;

import com.otd.configuration.util.FormattedTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class MainHomGetReq {
    private Long userId;
    private int year;
    private int month;
    private LocalDate date;

    public int getYear() {
        return FormattedTime.fetchYear();
    }
    public int getMonth() {
        return FormattedTime.fetchMonth();
    }
    public LocalDate getDate() {
        return FormattedTime.fetchDate();
    }
}
