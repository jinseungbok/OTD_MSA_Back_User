package com.otd.otd_user.application.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointHistoryResponseDTO {
    private UserPointDTO user;
    private List<PointHistoryDTO> pointHistory;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPointDTO {
        private String nickName;
        private int totalPoint;
    }
}