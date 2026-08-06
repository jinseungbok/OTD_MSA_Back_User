package com.otd.otd_admin.application.admin.model.statistics;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class InquiryCountRes {
    private String month;
    private Long inquiryCount;
}
