package com.otd.otd_pointshop.application.point.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class PointApiResponse<T> {
    private boolean success; // 성공 여부
    private String message; // 응답 메시지
    private T data; // 응답 데이터
    private Integer userCurrentPoint; // 현재 사용자 포인트

    // 생성자 1: 메시지만 포함
    public PointApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // 생성자 2: 데이터만 포함
    public PointApiResponse(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    // 생성자 3: 메시지, 데이터 포함
    public PointApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // 생성자 4: 메시지, 데이터, 포인트
    public PointApiResponse(boolean success, String message, T data, Integer userCurrentPoint) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.userCurrentPoint = userCurrentPoint;
    }

    // 팩토리 메서드 (일관성 확보)
    public static <T> PointApiResponse<T> success(T data) {
        return new PointApiResponse<>(true, "요청이 성공적으로 처리되었습니다.", data);
    }

    public static <T> PointApiResponse<T> success(String message, T data) {
        return new PointApiResponse<>(true, message, data);
    }

    public static <T> PointApiResponse<T> success(String message, T data, Integer userCurrentPoint) {
        return new PointApiResponse<>(true, message, data, userCurrentPoint);
    }

    public static <T> PointApiResponse<T> error(String message) {
        return new PointApiResponse<>(false, message, null);
    }
}