package com.swp.project.security.service;

import com.swp.project.entity.User;
import com.swp.project.security.CustomUserDetails;
import com.swp.project.security.SecurityUtils;
import com.swp.project.service.UserService;
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
    private final SecurityUtils securityUtils;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(request);
        String email = oauth2User.getAttribute("email");
        CustomUserDetails currentUser = securityUtils.getCurrentUser();
        if (currentUser != null){
            return linkToGoogle(oauth2User, currentUser);
        }
        User user;
        if(userService.isUserExistsByEmail(email)) {
            user = userService.getUserByEmail(email);
            if (!user.isEnabled()) {
                throw new OAuth2AuthenticationException(new OAuth2Error("account_disabled"));
            }
        } else {
            user = userService.registerWithGoogle(oauth2User.getAttribute("name"), email);
        }
        return new CustomUserDetails(user, oauth2User.getAttributes());
    }

    public OAuth2User linkToGoogle(OAuth2User oauth2User, CustomUserDetails currentUser) throws OAuth2AuthenticationException {
        String email = oauth2User.getAttribute("email");
        if (userService.isUserExistsByEmail(email)) {
            throw new OAuth2AuthenticationException(new OAuth2Error("account_already_linked"));
        } else {
            User user = userService.addGmailToExistedUser(currentUser.getUsername(), email);
            return new CustomUserDetails(user, oauth2User.getAttributes());
        }
    }

}
