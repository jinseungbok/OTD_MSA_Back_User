package com.otd.otd_user.application.term.model;

import com.otd.configuration.enumcode.model.EnumTermsType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TermsResponseDto {
    private Long termsId;
    private EnumTermsType type;
    private String typeDescription;
    private String title;
    private String content;
    private String version;
    private Boolean isRequired;
    private LocalDateTime effectiveDate;
    private LocalDateTime createdAt;
}