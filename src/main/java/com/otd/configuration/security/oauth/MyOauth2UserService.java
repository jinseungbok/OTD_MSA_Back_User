package com.otd.configuration.security.oauth;

import com.otd.configuration.enumcode.model.EnumChallengeRole;
import com.otd.otd_user.application.user.UserRepository;
import com.otd.configuration.enumcode.model.EnumUserRole;
import com.otd.configuration.model.JwtUser;
import com.otd.configuration.model.UserPrincipal;
import com.otd.configuration.security.SignInProviderType;
import com.otd.configuration.security.oauth.userinfo.Oauth2UserInfo;
import com.otd.configuration.security.oauth.userinfo.Oauth2UserInfoFactory;
import com.otd.otd_user.entity.User;
import com.otd.otd_user.entity.UserRole;
import com.otd.otd_user.entity.UserRoleIds;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;



@Slf4j
@Service
@RequiredArgsConstructor
public class MyOauth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final Oauth2UserInfoFactory oauth2UserInfoFactory;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) {
        try {
            return process(req);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }


    private OAuth2User process(OAuth2UserRequest req) {
        OAuth2User oAuth2User = super.loadUser(req);

        SignInProviderType signInProviderType = SignInProviderType.valueOf(req.getClientRegistration()
                .getRegistrationId()
                .toUpperCase());

        Oauth2UserInfo oauth2UserInfo = oauth2UserInfoFactory.getOauth2UserInfo(signInProviderType, oAuth2User.getAttributes());


        User user = userRepository.findByUidAndProviderType(oauth2UserInfo.getId(), signInProviderType);
        if(user == null) { // 최초 로그인 상황 > 회원가입 처리
            user = new User();
            user.setUid(oauth2UserInfo.getId());
            user.setProviderType(signInProviderType);
            user.setUpw("");
            user.setNickName(oauth2UserInfo.getName());
            user.setPic(oauth2UserInfo.getProfileImageUrl());
            user.setOnboardingCompleted(0);


            List<UserRole> userRoles = new ArrayList<>(1);
            UserRoleIds ids = new UserRoleIds(user.getUserId(), EnumUserRole.USER_2, EnumChallengeRole.TBD);  // ⭐ USER_1로 설정 (온보딩 전)

            UserRole userRole = new UserRole(ids, user);
            userRoles.add(userRole);

            user.setUserRoles(userRoles);
            userRepository.save(user);

            log.info("신규 소셜 회원가입 완료 - UserId: {}, Provider: {}, OnboardingCompleted: {}",
                    user.getUserId(), signInProviderType, user.getOnboardingCompleted());
        }

        List<EnumUserRole> roles = user.getUserRoles().stream()
                .map(item -> item.getUserRoleIds().getRoleCode())
                .toList();

        String nickName = user.getNickName() == null ? user.getUid() : user.getNickName();

        // ⭐ 온보딩 정보 포함하여 OAuth2JwtUser 생성
        String providerTypeCode = user.getProviderType() != null
                ? user.getProviderType().getCode()
                : null;

        JwtUser jwtUser = new OAuth2JwtUser(
                nickName,
                user.getPic(),
                providerTypeCode,
                user.getOnboardingCompleted(),
                user.getUserId(),
                roles
        );

        log.info("OAuth2 인증 완료 - UserId: {}, NickName: {}, ProviderType: {}, OnboardingCompleted: {}",
                user.getUserId(), nickName, providerTypeCode, user.getOnboardingCompleted());

        UserPrincipal myUserDetails = new UserPrincipal(jwtUser);
        return myUserDetails;
    }
}