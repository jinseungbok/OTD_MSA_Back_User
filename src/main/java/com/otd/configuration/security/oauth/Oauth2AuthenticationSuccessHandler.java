package com.otd.configuration.security.oauth;


import com.otd.configuration.constants.ConstJwt;
import com.otd.configuration.constants.ConstOAuth2;
import com.otd.configuration.jwt.JwtTokenManager;
import com.otd.configuration.model.JwtUser;
import com.otd.configuration.model.UserPrincipal;
import com.otd.configuration.util.CookieUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class Oauth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final Oauth2AuthenticationRequestBasedOnCookieRepository repository;
    private final JwtTokenManager jwtTokenManager;
    private final ConstOAuth2 constOAuth2;
    private final CookieUtils cookieUtils;
    private final ConstJwt constJwt;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication auth)
            throws IOException, ServletException {
        if(res.isCommitted()) {
            log.error("onAuthenticationSuccess called with a committed response {}", res);
            return;
        }
        String targetUrl = this.determineTargetUrl(req, res, auth);
        log.info("onAuthenticationSuccess targetUrl={}", targetUrl);
        clearAuthenticationAttributes(req, res);
        getRedirectStrategy().sendRedirect(req, res, targetUrl);
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest req, HttpServletResponse res, Authentication auth) {
        String redirectUrl = cookieUtils.getValue(req, constOAuth2.redirectUriParamCookieName, String.class);

        log.info("determineTargetUrl > getDefaultTargetUrl(): {}", getDefaultTargetUrl());

        String targetUrl = redirectUrl == null ? getDefaultTargetUrl() : redirectUrl;

        // 쿼리스트링 생성을 위한 준비과정
        UserPrincipal myUserDetails = (UserPrincipal) auth.getPrincipal();
        OAuth2JwtUser oauth2JwtUser = (OAuth2JwtUser)myUserDetails.getJwtUser();
        JwtUser jwtUser = new JwtUser(oauth2JwtUser.getSignedUserId(), oauth2JwtUser.getRoles());

        // AT, RT 생성 후 쿠키에 저장
        jwtTokenManager.issue(res, jwtUser);

        // ⭐ 온보딩 정보 추가
        String providerType = oauth2JwtUser.getProviderType() != null
                ? oauth2JwtUser.getProviderType()
                : "";
//        Integer onboardingCompleted = oauth2JwtUser.getOnboardingCompleted() != null
//                ? oauth2JwtUser.getOnboardingCompleted()
//                : 0;

        log.info("OAuth2 로그인 성공 - UserId: {}, ProviderType: {}, OnboardingCompleted: {}",
                oauth2JwtUser.getSignedUserId(), providerType, oauth2JwtUser.getOnboardingCompleted());

        /*
            쿼리스트링 생성
            targetUrl: /fe/redirect
            userId: 20
            nickName: 홍길동
            pic: abc.jpg
            providerType: KAKAO         ⭐ 추가
            onboardingCompleted: 0      ⭐ 추가

            결과: "fe/redirect?user_id=20&nick_name=홍길동&pic=abc.jpg&provider_type=KAKAO&onboarding_completed=0"
         */
        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("user_id", oauth2JwtUser.getSignedUserId())
                .queryParam("nick_name", oauth2JwtUser.getNickName()).encode()
                .queryParam("pic", oauth2JwtUser.getPic())
                .queryParam("provider_type", providerType)           // ⭐ 추가
                .queryParam("onboarding_completed", oauth2JwtUser.getOnboardingCompleted())  // ⭐ 추가
                .build()
                .toUriString();
    }

    private void clearAuthenticationAttributes(HttpServletRequest req, HttpServletResponse res) {
        super.clearAuthenticationAttributes(req);
    }
}