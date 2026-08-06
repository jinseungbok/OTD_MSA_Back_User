package com.otd.otd_user.application.onboarding;

import com.otd.configuration.enumcode.model.EnumChallengeRole;
import com.otd.configuration.enumcode.model.EnumUserRole;
import com.otd.otd_user.application.onboarding.model.OnboardingRequest;
import com.otd.otd_user.application.term.TermsService;
import com.otd.otd_user.application.user.UserRepository;
import com.otd.otd_user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingService {

    private final UserRepository userRepository;
    private final TermsService termsService;

    @Transactional
    public void completeOnboarding(Long userId, OnboardingRequest request,
                                   HttpServletRequest httpRequest) {
        log.info("온보딩 완료 처리 시작 - UserId: {}", userId);


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (user.getOnboardingCompleted() != null && user.getOnboardingCompleted() == 1) {
            log.warn("이미 온보딩이 완료된 사용자입니다 - UserId: {}", userId);
            throw new RuntimeException("이미 온보딩이 완료되었습니다.");
        }

        if (user.getProviderType() == null) {
            throw new RuntimeException("소셜 로그인 사용자만 온보딩이 필요합니다.");
        }

        // 4. 약관 동의 처리
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        termsService.agreeToTerms(
                userId,
                request.getAgreedTermsIds(),
                ipAddress,
                userAgent
        );


        boolean hasAgreedAll = termsService.hasAgreedToAllRequiredTerms(userId);
        if (!hasAgreedAll) {
            throw new RuntimeException("필수 약관에 모두 동의해야 합니다.");
        }


        EnumChallengeRole challengeRole =
                EnumChallengeRole.fromCode(request.getSurveyScore());
        log.info("설문 점수: {}, 산출 등급: {}", request.getSurveyScore(), challengeRole);


        updateUserChallengeRole(user, challengeRole);


        user.setOnboardingCompleted(1);

        userRepository.save(user);

        log.info("온보딩 완료 - UserId: {}, ChallengeRole: {}", userId, challengeRole);
    }


     //온보딩 상태 조회

    @Transactional(readOnly = true)
    public Boolean getOnboardingStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));


        return user.getOnboardingCompleted() != null && user.getOnboardingCompleted() == 1;
    }


    //UserRole의 챌린지 등급만 업데이트

    private void updateUserChallengeRole(User user, EnumChallengeRole challengeRole) {
        user.getUserRoles().clear();

        List<EnumUserRole> userRoles = new ArrayList<>();
        userRoles.add(EnumUserRole.USER_2);

        user.addUserRoles(userRoles, challengeRole);

        log.info("UserRole 업데이트 - UserId: {}, UserRole: USER_2, ChallengeRole: {}",
                user.getUserId(), challengeRole);
    }

    /**
     * 클라이언트 IP 추출
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}