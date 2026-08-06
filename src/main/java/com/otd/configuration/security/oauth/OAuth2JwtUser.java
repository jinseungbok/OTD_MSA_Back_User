package com.otd.configuration.security.oauth;

import com.otd.configuration.enumcode.model.EnumUserRole;
import com.otd.configuration.model.JwtUser;
import lombok.Getter;

import java.util.List;


@Getter
public class OAuth2JwtUser extends JwtUser {
    private final String nickName;
    private final String pic;
    private final String providerType;
    private final Integer onboardingCompleted;


    public OAuth2JwtUser(String nickName, String pic, long signedUserId, List<EnumUserRole> roles) {
        this(nickName, pic, null, null, signedUserId, roles);
    }


    public OAuth2JwtUser(String nickName, String pic, String providerType, Integer onboardingCompleted,
                         long signedUserId, List<EnumUserRole> roles) {
        super(signedUserId, roles);
        this.nickName = nickName;
        this.pic = pic;
        this.providerType = providerType;
        this.onboardingCompleted = onboardingCompleted;
    }
}