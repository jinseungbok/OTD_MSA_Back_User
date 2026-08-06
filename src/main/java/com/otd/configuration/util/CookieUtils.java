package com.otd.configuration.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.Arrays;
import java.util.Base64;

//쿠키에 데이터 담고 빼고 할 때 사용하는 객체
@Slf4j
@Component //빈등록
@RequiredArgsConstructor
public class CookieUtils {
    private final Environment environment;

    /*
    response: 쿠키를 담을 때 필요함
    name: 쿠키에 담을 벨류의 레이블(키값)
    value: 쿠키에 담을 벨류
    maxAge: 쿠키에 담긴 벨류의 유효 기간
    path: 설정한 경로에 요청이 갈 때만 쿠키가 전달된다.
     */
    public void setCookie(HttpServletResponse res, String name, Object value, int maxAge, String path, String domain) {
        this.setCookie(res, name, serializeObject(value), maxAge, path, domain);
    }
    public void setCookie(HttpServletResponse response, String name, String value, int maxAge, String path, String domain) {
        /*
            쿠버네티스에서 실행되면 프로파일 2개로 실행(prod, kubernetes)
            prod는 도커 이미지를 만들 때 실행명령어에 prod로 서버를 기동하라는 내용 포함되어 있음
            kubernetes는 쿠버네티스가 서버 기동할 때 포함 시킴
         */
        String[] activeProfiles = environment.getActiveProfiles();

        if(domain != null && Arrays.asList(activeProfiles).contains("prod")) { //프로파일에 prod가 포함되어 있는지 확인
            //쿠키 생성 방법 (1) ResponseCookie.from 스태틱 메소드 이용
            log.info("CookieUtils - 프로파일에 prod가 있음");
            ResponseCookie cookie = ResponseCookie.from(name, value)
                    .path(path)
                    .maxAge(maxAge)
                    .httpOnly(true)
                    .domain(domain)
                    .secure(true) //https일 때만 쿠키 전송된다.
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        } else {
            //쿠키 생성 방법 (2) Cookie 객체 생성
            log.info("CookieUtils - 기본 프로파일");
            Cookie cookie = new Cookie(name, value);
            cookie.setPath(path);
            cookie.setMaxAge(maxAge);
            cookie.setHttpOnly(true); //보안 쿠키 설정
            response.addCookie(cookie);
        }
    }

    public String getValue(HttpServletRequest request, String name) {

        Cookie cookie = getCookie(request, name);

//        log.info("cookie  : " + cookie.getValue());
        if(cookie == null) { return null; }
        return cookie.getValue();
    }

    public <T> T getValue(HttpServletRequest req, String name, Class<T> valueType) {
        Cookie cookie = getCookie(req, name);
        if (cookie == null) { return null; }
        if(valueType == String.class) {
            return (T) cookie.getValue();
        }
        return deserializeCookie(cookie, valueType);
    }

    private String serializeObject(Object obj) {
        return Base64.getUrlEncoder().encodeToString( SerializationUtils.serialize(obj) );
    }

    //역직렬화, 문자열값을 객체로 변환
    private <T> T deserializeCookie(Cookie cookie, Class<T> valueType) {
        return valueType.cast(
                SerializationUtils.deserialize( Base64.getUrlDecoder().decode(cookie.getValue()) )
        );
    }

    private Cookie getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies(); //쿠키가 req에 여러개가 있을 수 있기 때문에 배열로 리턴
        log.info("cookie  : " + cookies);
        if (cookies != null && cookies.length > 0) { //쿠키에 뭔가 담겨져 있다면
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) { //쿠키에 담긴 이름이 같은게 있다면
                    return cookie; //해당 쿠키를 리턴
                }
            }
        }
        return null;
    }

    public void deleteCookie(HttpServletResponse response, String name, String path, String domain) {
        setCookie(response, name, null, 0, path, domain);
    }


}
