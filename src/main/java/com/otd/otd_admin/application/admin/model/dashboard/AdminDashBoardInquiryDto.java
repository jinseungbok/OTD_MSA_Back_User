package com.otd.otd_admin.application.admin.model.dashboard;

import com.otd.otd_user.entity.Inquiry;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class AdminDashBoardInquiryDto {
    private int totalInquiryCount;
    private List<Inquiry> recentInquiryList;
    private int unansweredInquiryCount;
    private Double avgRepliedTime;
    private Double responseRate;
}
