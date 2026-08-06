package com.otd.otd_user.application.email.model;

import com.otd.configuration.enumcode.model.EnumInquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryDetailRes {
    private Long id;
    private String subject;
    private String content;
    private String senderName;
    private String senderEmail;
    private EnumInquiryStatus status;
    private String reply;
    private LocalDateTime replyAt;
    private String statusTitle;
    private LocalDateTime createdAt;
}