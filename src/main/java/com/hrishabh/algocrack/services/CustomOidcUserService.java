package com.hrishabh.algocrack.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

        // Use existing logic from your OAuth2 service
        OAuth2User oauth2User = customOAuth2UserService.loadUser(userRequest);

        return new DefaultOidcUser(
                oauth2User.getAuthorities(),
                userRequest.getIdToken(),
                new OidcUserInfo(oauth2User.getAttributes()),
                "email" // or "sub" if preferred
        );
    }
}
