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
public class AgreementResponseDto {
    private Long agreementId;
    private Long termsId;
    private EnumTermsType enumTermsType;
    private String termsTitle;
    private Boolean agreed;
    private LocalDateTime agreedAt;
}