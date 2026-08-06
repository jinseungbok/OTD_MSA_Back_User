package com.otd.configuration.feignclient;


import com.otd.configuration.constants.ConstKakaoPay;
import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class KakaoPayClientConfiguration {

    private final ConstKakaoPay constKakaoPay;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> requestTemplate.header("Content-Type", "application/json")
                                                 .header(constKakaoPay.authorizationName, String.format("SECRET_KEY %s", constKakaoPay.secretKey));
    }
}
