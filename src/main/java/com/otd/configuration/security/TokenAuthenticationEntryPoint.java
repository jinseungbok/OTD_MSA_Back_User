package com.otd.configuration.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class TokenAuthenticationEntryPoint implements AuthenticationEntryPoint {
    /*
    인증 문제가 발생되었을 때 commence 메소드 호출된다!
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        // 표준화된 JSON 오브젝트로 응답 (문자만/불리언 리터럴 X)
        String body = """
      {"success":false,"code":"UNAUTHORIZED","message":"Authentication required"}
    """;
        response.getWriter().write(body);
        response.getWriter().flush();
        // 여기서 끝 (체인 진행 X)
    }
}
