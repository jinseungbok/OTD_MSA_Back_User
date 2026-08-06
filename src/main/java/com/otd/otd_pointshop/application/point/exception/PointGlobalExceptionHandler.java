package com.otd.otd_pointshop.application.point.exception;

import com.otd.otd_pointshop.application.point.model.PointApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.otd.otd_pointshop")
public class PointGlobalExceptionHandler {

    // 로그인 필요 / 세션 만료 시
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<PointApiResponse<?>> handleUnauthorized(IllegalStateException e) {
        log.warn("401 Unauthorized: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new PointApiResponse<>(false, "로그인이 필요합니다."));
    }

    // 접근 권한 없음
    @ExceptionHandler(IllegalAccessException.class)
    public ResponseEntity<PointApiResponse<?>> handleForbidden(IllegalAccessException e) {
        log.warn("403 Forbidden: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new PointApiResponse<>(false, "접근 권한이 없습니다."));
    }

    // 나머지 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<PointApiResponse<?>> handleException(Exception e) {
        log.error("500 Internal Server Error: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new PointApiResponse<>(false, "서버 오류: " + e.getMessage()));
    }
}
