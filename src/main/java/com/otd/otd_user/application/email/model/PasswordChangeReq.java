package com.otd.otd_user.application.email.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeReq {
    @NotNull(message = "현재 비밀번호는 필수입니다.")
    private String currentPassword;

    @NotNull(message = "새 비밀번호는 필수입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{10,}$")
    private String newPassword;

    @NotNull(message = "비밀번호 확인은 필수입니다.")
    private String confirmPassword;
}