package com.otd.otd_user.application.user.model;

import com.otd.configuration.enumcode.model.EnumChallengeRole;
import com.otd.configuration.enumcode.model.EnumUserRole;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


import java.util.List;

@Data
public class UserJoinReq{
    @Column(nullable = false)
    @Pattern(regexp = "^[A-Za-z0-9_]{4,50}$", message = "아이디는 영어, 숫자, 언더바로만 구성되어야 하며 4~50자까지 작성할 수 있습니다.")
    private String uid;

    @Column(nullable = false)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{10,}$",
            message = "비밀번호는 10자 이상이어야 합니다.")
    private String upw;

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String birthDate;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String nickname;

    private String pic;

    private List<EnumUserRole> roles;
    private int surveyAnswers; // 챌린지 설문 총 점수

    private List<Long> agreedTermsIds; //약관관련
    private String ipAddress;
    private String userAgent;
}