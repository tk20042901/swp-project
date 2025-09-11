package com.swp.project.security;

import com.swp.project.entity.User;
import com.swp.project.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(request);
        String email = oauth2User.getAttribute("email");
        User user;
        if(userService.isUserExistsByEmail(email)) {
            user = userService.getUserByEmail(email);
            if (!user.isEnabled()) {
                throw new OAuth2AuthenticationException(new OAuth2Error("account_disabled"));
            }
        } else {
            user = userService.registerWithGoogle(email);
        }
        return new CustomUserDetails(user, oauth2User.getAttributes());
    }

}
