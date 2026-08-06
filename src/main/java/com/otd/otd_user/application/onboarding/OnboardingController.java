package com.otd.otd_user.application.onboarding;

import com.otd.configuration.model.UserPrincipal;
import com.otd.otd_user.application.onboarding.model.OnboardingRequest;
import com.otd.otd_user.application.term.model.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/OTD/onboarding")
@RequiredArgsConstructor
@Slf4j
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping("/complete")
    public ResponseEntity<ApiResponse<String>> completeOnboarding(
            @Valid @RequestBody OnboardingRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest httpRequest) {


        log.info("Authenticated userId: {}", userPrincipal.getSignedUserId());

        if (userPrincipal.getSignedUserId() == null) {
            log.warn("userId is null! 인증이 안 되어 있습니다.");
            return ResponseEntity.status(401).body(ApiResponse.error("인증 필요"));
        }

        try {
            onboardingService.completeOnboarding(userPrincipal.getSignedUserId(), request, httpRequest);
            return ResponseEntity.ok(ApiResponse.success("온보딩이 완료되었습니다."));
        } catch (Exception e) {
            log.error("온보딩 완료 실패 - UserId: {}", userPrincipal.getSignedUserId(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 온보딩 상태 조회
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> getOnboardingStatus(
            @AuthenticationPrincipal Long userId) {

        try {
            Boolean status = onboardingService.getOnboardingStatus(userId);
            return ResponseEntity.ok(ApiResponse.success(status));
        } catch (Exception e) {
            log.error("온보딩 상태 조회 실패 - UserId: {}", userId, e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}