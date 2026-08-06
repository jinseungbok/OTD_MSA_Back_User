package com.otd.otd_admin.application.admin.model.statistics;

import com.otd.otd_user.entity.Inquiry;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class AdminStatisticsInquiryDto {
    private Double responseRate;
    private List<InquiryCountRes> inquiryCount;
}
