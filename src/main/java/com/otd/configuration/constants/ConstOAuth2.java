package com.otd.configuration.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "constants.oauth2")
public class ConstOAuth2 {
    public final String baseUri;
    public final String authorizationRequestCookieName;
    public final String redirectUriParamCookieName;
    public final String redirectionBaseUri;
    public final int cookieExpirySeconds;
    public final List<String> authorizedRedirectUris;
}
