package com.otd.configuration.jwt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.otd.configuration.constants.ConstJwt;
import com.otd.configuration.model.JwtUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {
    private final ObjectMapper objectMapper; //Jackson 라이브러리 (JSON to Object, Object to JSON)
    private final ConstJwt constJwt;
    private final SecretKey secretKey;

    public JwtTokenProvider(ObjectMapper objectMapper, ConstJwt constJwt) {
        this.objectMapper = objectMapper;
        this.constJwt = constJwt;
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(constJwt.secretKey));
    }

    // JWT 토큰 생성
    public String generateToken(JwtUser jwtUser, long tokenValidityMilliSeconds) {
        Date now = new Date();
        return Jwts.builder()
                //header
                .header().type(constJwt.bearerFormat)
                .and()

                //payload
                .issuer(constJwt.issuer)
                .issuedAt(now) //발행일시(토큰 생성일시)
                .expiration(new Date(now.getTime() + tokenValidityMilliSeconds)) //만료일시(토큰 만료일시)
                .claim(constJwt.claimKey, makeClaimByUserToJson(jwtUser)) //커스텀 클레임

                //signature
                .signWith(secretKey)
                .compact();
    }

    private String makeClaimByUserToJson(JwtUser jwtUser) {
        try {
            return objectMapper.writeValueAsString(jwtUser);
        } catch (JsonProcessingException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "토큰 생성 에러 발생" );
        }
    }

    public JwtUser getJwtUserFromToken(String token) {
        try {
            Claims claims = getClaims(token);
            String json = claims.get(constJwt.claimKey, String.class);
            return objectMapper.readValue(json, JwtUser.class);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "토큰 문제 발생" );
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 소셜 온보딩용 레지스터 토큰 생성 (0.12.x 버전)
     *
     * @param nickname 소셜에서 받은 닉네임
     * @param profileImageUrl 소셜에서 받은 프로필 이미지 URL
     * @param providerType 소셜 제공자 타입 (KAKAO, GOOGLE 등)
     * @param providerId 고유 식별자 (KAKAO_1234567890 형태)
     * @param expirationMillis 만료 시간 (밀리초)
     * @return 생성된 레지스터 토큰
     */
    public String generateRegisterToken(
            String nickname,
            String profileImageUrl,
            String providerType,
            String providerId,
            long expirationMillis) {

        Date now = new Date();
        return Jwts.builder()
                .claim("nickname", nickname)
                .claim("profileImageUrl", profileImageUrl)
                .claim("providerType", providerType)
                .claim("providerId", providerId)
                .claim("tokenType", "social_register")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMillis))  // ✅ 0.12.x: setExpiration → expiration
                .signWith(secretKey)
                .compact();
    }

    /**
     *
     * @param token JWT 토큰
     * @param claimName 추출할 클레임 이름
     * @param type 클레임의 타입
     * @return 추출된 클레임 값
     */
    public <T> T getClaimFromToken(String token, String claimName, Class<T> type) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get(claimName, type);
        } catch (Exception e) {
            log.error("토큰 파싱 실패: {}", e.getMessage());
            throw new RuntimeException("토큰 파싱 실패", e);
        }
    }
}