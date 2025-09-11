package com.swp.project.config;

import com.swp.project.filter.CaptchaValidationFilter;
import com.swp.project.security.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CaptchaValidationFilter captchaValidationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;

    // Đường dẫn đến trang đăng nhập
    private static final String LOGIN_PAGE = "/login";

    // Trang chủ
    private static final String HOME_URL = "/";

    // Đường dẫn không yêu cầu đăng nhập
    private static final String[] PUBLIC_MATCHERS = {
            "/",
            "/register/**",
            "/login/**",
            "/css/**",
            "/js/**",
            "/forgot-password/**",
            "/verify-otp/**"
    };

    // Đường dẫn dành riêng cho vai trò Admin
    private static final String[] ADMIN_MATCHERS = {
            "/admin/**"
    };

    // Thời gian remember-me (s)
    private static final int REMEMBER_ME_VALIDITY = Integer.MAX_VALUE;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(captchaValidationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(i -> i
                        .requestMatchers(PUBLIC_MATCHERS).permitAll()
                        .requestMatchers(ADMIN_MATCHERS).hasAuthority("Admin")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(i -> i
                                .accessDeniedPage("/access-denied")
                )
                .formLogin(i -> i
                        .loginPage(LOGIN_PAGE)
                        .usernameParameter("email")
                        .failureHandler(loginFailureHandler())
                        .defaultSuccessUrl(HOME_URL,true)
                )
                .oauth2Login(i -> i
                        .loginPage(LOGIN_PAGE)
                        .failureHandler(oauth2FailureHandler())
                        .defaultSuccessUrl(HOME_URL, true)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                )
                .rememberMe(i -> i
                        .tokenValiditySeconds(REMEMBER_ME_VALIDITY)
                );

        return http.build();
    }

    public AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, exception) -> {
            if (exception instanceof BadCredentialsException
                    || exception instanceof UsernameNotFoundException) {
                response.sendRedirect("/login?incorrect_email_or_password");
            } else if (exception instanceof DisabledException){
                response.sendRedirect("/login?account_disabled");
            } else {
                response.sendRedirect("/login?unknown_error");
            }
        };
    }

    public AuthenticationFailureHandler oauth2FailureHandler() {
        return (request, response, exception) -> {
            if (exception instanceof OAuth2AuthenticationException authEx) {
                String error = authEx.getError().getErrorCode();
                if (error.equals("account_disabled")) {
                    response.sendRedirect("/login?account_disabled");
                } else {
                    response.sendRedirect("/login?unknown_error");
                }
            } else {
                response.sendRedirect("/login?unknown_error");
            }
        };
    }
}
