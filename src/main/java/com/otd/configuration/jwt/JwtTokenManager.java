package com.otd.configuration.jwt;

import com.otd.configuration.constants.ConstJwt;
import com.otd.configuration.model.JwtUser;
import com.otd.configuration.model.UserPrincipal;
import com.otd.configuration.util.CookieUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

//JWT 총괄 책임자
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenManager {
    private final ConstJwt constJwt; //설정 내용(문자열)
    private final CookieUtils cookieUtils; //쿠키 관련
    private final JwtTokenProvider jwtTokenProvider; //JWT 관련

    public String issue(HttpServletResponse response, JwtUser jwtUser) {
        String refreshToken = generateRefreshToken(jwtUser);
        setAccessTokenInCookie(response, jwtUser);
        setRefreshTokenInCookie(response, jwtUser);
        return refreshToken;
    }

    public String generateAccessToken(JwtUser jwtUser) {
        return jwtTokenProvider.generateToken(jwtUser, constJwt.getAccessTokenValidityMilliseconds());
    }

    public void setAccessTokenInCookie(HttpServletResponse response, JwtUser jwtUser) {
        setAccessTokenInCookie(response, generateAccessToken(jwtUser));
    }

    public void setAccessTokenInCookie(HttpServletResponse response, String accessToken) {
        cookieUtils.setCookie(response
                , constJwt.getAccessTokenCookieName()
                , accessToken
                , constJwt.getAccessTokenCookieValiditySeconds()
                , constJwt.getAccessTokenCookiePath() ,constJwt.getDomain()
        );
    }

    public String getAccessTokenFromCookie(HttpServletRequest request) {
        return cookieUtils.getValue(request, constJwt.getAccessTokenCookieName());
    }

    public void deleteAccessTokenInCookie(HttpServletResponse response) {
        cookieUtils.deleteCookie(response, constJwt.getAccessTokenCookieName(), constJwt.getAccessTokenCookiePath(), constJwt.getDomain());
    }

    public String generateRefreshToken(JwtUser jwtUser) {
        return jwtTokenProvider.generateToken(jwtUser, constJwt.getRefreshTokenValidityMilliseconds());
    }

    public void setRefreshTokenInCookie(HttpServletResponse response, JwtUser jwtUser) {
        setRefreshTokenInCookie(response, generateRefreshToken(jwtUser));
    }

    public void setRefreshTokenInCookie(HttpServletResponse response, String refreshToken) {
        cookieUtils.setCookie(response, constJwt.getRefreshTokenCookieName(), refreshToken, constJwt.getRefreshTokenCookieValiditySeconds(), constJwt.getRefreshTokenCookiePath(),constJwt.getDomain());
    }

    public void deleteRefreshTokenInCookie(HttpServletResponse response) {
        cookieUtils.deleteCookie(response, constJwt.getRefreshTokenCookieName(), constJwt.getRefreshTokenCookiePath(),constJwt.getDomain());
    }

    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        log.info("request cookie : " + request.getCookies());
        return cookieUtils.getValue(request, constJwt.getRefreshTokenCookieName());
    }

    public JwtUser getJwtUserFromToken(String token) {
        return jwtTokenProvider.getJwtUserFromToken(token);
    }

    private void deleteSocialLogin(HttpServletResponse response) {
        cookieUtils.deleteCookie(response, "JSESSIONID", null, constJwt.getDomain());
        cookieUtils.deleteCookie(response, "Authorization", null, constJwt.getDomain());
        cookieUtils.deleteCookie(response, "RefreshToken", null, constJwt.getDomain());
    }

    public void reissue(HttpServletRequest request, HttpServletResponse response) {
        //request에서 refreshToken을 얻는다.
        String refreshToken = getRefreshTokenFromCookie(request);

        //refreshToken에서 jwtUser를 만든다.
        JwtUser jwtUser = getJwtUserFromToken(refreshToken);

        //jwtUser로 accessToken을 발행한다.
        String accessToken = generateAccessToken(jwtUser);

        //accessToken을 쿠키에 담는다.
        setAccessTokenInCookie(response, accessToken);
    }

    public void logout(HttpServletResponse response) {
        deleteAccessTokenInCookie(response);
        deleteRefreshTokenInCookie(response);
        deleteSocialLogin(response);
    }

    public Authentication getAuthentication(HttpServletRequest request) {
        String accessToken = getAccessTokenFromCookie(request);
        String refreshToken = getRefreshTokenFromCookie(request);
        log.info("accessToken: {}", accessToken);
        log.info("refreshToken: {}", refreshToken);
        if(accessToken !=null){
            JwtUser jwtUser = getJwtUserFromToken(accessToken);
            UserPrincipal userPrincipal = new UserPrincipal(jwtUser);
            return new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        }
        else if(refreshToken != null){
            JwtUser jwtUser = getJwtUserFromToken(refreshToken);
            log.info(jwtUser.toString());
            UserPrincipal userPrincipal = new UserPrincipal(jwtUser);
            return new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        }
        else
            return null;
    }

    // ============================================
    // 소셜 온보딩용 레지스터 토큰 메서드들
    // ============================================

    /**
     * 소셜 온보딩용 레지스터 토큰 생성
     * JwtTokenProvider를 활용하여 토큰 생성
     */
    public String generateSocialRegisterToken(
            String nickname,
            String profileImageUrl,
            String providerType,
            String providerId,
            long expirationMillis) {

        // JwtTokenProvider에 레지스터 토큰 생성 메서드 추가 필요
        return jwtTokenProvider.generateRegisterToken(
                nickname,
                profileImageUrl,
                providerType,
                providerId,
                expirationMillis
        );
    }

    /**
     * 레지스터 토큰에서 닉네임 추출
     */
    public String getNicknameFromRegisterToken(String token) {
        return jwtTokenProvider.getClaimFromToken(token, "nickname", String.class);
    }

    /**
     * 레지스터 토큰에서 프로필 이미지 URL 추출
     */
    public String getProfileImageUrlFromRegisterToken(String token) {
        return jwtTokenProvider.getClaimFromToken(token, "profileImageUrl", String.class);
    }

    /**
     * 레지스터 토큰에서 제공자 타입 추출
     */
    public String getProviderTypeFromRegisterToken(String token) {
        return jwtTokenProvider.getClaimFromToken(token, "providerType", String.class);
    }

    /**
     * 레지스터 토큰에서 제공자 ID 추출
     */
    public String getProviderIdFromRegisterToken(String token) {
        return jwtTokenProvider.getClaimFromToken(token, "providerId", String.class);
    }
}