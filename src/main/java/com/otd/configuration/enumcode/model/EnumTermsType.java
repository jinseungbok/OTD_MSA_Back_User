package com.otd.configuration.enumcode.model;

public enum EnumTermsType {
    SERVICE("서비스 이용약관"),
    PRIVACY("개인정보 처리방침"),
    MARKETING("마케팅 수신동의"),
    LOCATION("위치기반 서비스 이용약관");

    private final String description;

    EnumTermsType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}