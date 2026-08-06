package com.otd.otd_user.application.onboarding.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnboardingRequest {
    private List<Long> agreedTermsIds;
    private Integer surveyScore;
}