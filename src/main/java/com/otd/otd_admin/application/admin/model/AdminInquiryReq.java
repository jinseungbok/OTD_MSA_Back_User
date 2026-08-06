package com.otd.otd_admin.application.admin.model;

import com.otd.configuration.enumcode.model.EnumInquiryStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AdminInquiryReq {
    private long id;
    private Long adminId;
    private String repliedNickName;
    private String reply;
    private EnumInquiryStatus status;
}
