package com.otd.configuration.security;

import com.otd.configuration.constants.ConstOAuth2;
import com.otd.configuration.enumcode.model.EnumUserRole;
import com.otd.configuration.security.oauth.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

/*
@Configuration - bean 등록, Bean 메소드가 있다.
Bean 메소드는 무조건 싱글톤으로 처리된다.
 */
@Configuration
@RequiredArgsConstructor
public class WebSecurityConfiguration {

    private final TokenAuthenticationFilter tokenAuthenticationFilter;
    private final TokenAuthenticationEntryPoint tokenAuthenticationEntryPoint;

    private final Oauth2AuthenticationRequestBasedOnCookieRepository repository;
    private final Oauth2AuthenticationSuccessHandler authenticationSuccessHandler;
    private final Oauth2AuthenticationFailureHandler authenticationFailureHandler;
    private final MyOauth2UserService myOauth2UserService;
    private final ConstOAuth2 constOAuth2;

    //Bean 메소드
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //람다식
        return http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //security가 session을 사용하지 않는다.
                .httpBasic(httpBasicSpec -> httpBasicSpec.disable()) //시큐리티가 제공해주는 인증 처리 -> 사용 안 함
                .formLogin(formLoginSpec -> formLoginSpec.disable()) //시큐리티가 제공해주는 인증 처리 -> 사용 안 함
                .csrf(csrfSpec -> csrfSpec.disable()) // BE - csrf라는 공격이 있는데 공격을 막는 것이 기본으로 활성화 되어 있는데
                // 세션을 이용한 공격이다. 세션을 어차피 안 쓰니까 비활성화
                .authorizeHttpRequests(req -> req
                        .requestMatchers(HttpMethod.POST, "/api/OTD/user/logout").authenticated()
                        .requestMatchers("/api/OTD/user/profile" , "/api/OTD/user/profile/pic").authenticated()
                        .requestMatchers("/api/OTD/email/sendMunhe").authenticated()
                        .requestMatchers("/api/OTD/pointshop/image/**", "/OTD/pointshop/image/**" ).authenticated()
                        .requestMatchers( "/api/OTD/user/logout",
                                "/api/OTD/user/reissue",
                                "/api/OTD/user/join",
                                "/api/OTD/user/login",
                                "/api/OTD/user/**"
                                ,"/home/green/download/challenge/**").permitAll()
                        .anyRequest().permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/OTD/user/logout") // 기본은 POST, 아래에서 GET 허용 예시
                        .logoutSuccessHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_OK))
                )       .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2.authorizationEndpoint( auth -> auth.baseUri(constOAuth2.baseUri)
                                        .authorizationRequestRepository(repository)
                                )
                                .redirectionEndpoint( redirection -> redirection.baseUri(constOAuth2.redirectionBaseUri) )
                                .userInfoEndpoint( userInfo -> userInfo.userService(myOauth2UserService) )
                                .successHandler( authenticationSuccessHandler )
                                .failureHandler( authenticationFailureHandler )
                )
                .addFilterBefore(new Oauth2AuthenticationCheckRedirectUriFilter(constOAuth2), OAuth2AuthorizationRequestRedirectFilter.class)
                //.logout(logout -> logout.logoutUrl("/api/user/sign-out").deleteCookies("JSESSIONID", "Authorization", "RefreshToken"))
                .exceptionHandling(e -> e.authenticationEntryPoint(tokenAuthenticationEntryPoint))
                .build();
    }




    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

}