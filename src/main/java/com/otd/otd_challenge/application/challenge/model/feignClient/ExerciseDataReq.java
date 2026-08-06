package com.otd.otd_challenge.application.challenge.model.feignClient;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ExerciseDataReq {
    private Long userId;
    private Long recordId;
    private String name;
    private Double record;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate recordDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate today;

    private int count;
    private int totalKcal;
}
