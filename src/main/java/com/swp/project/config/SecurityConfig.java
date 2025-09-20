package com.swp.project.config;

import com.swp.project.filter.CaptchaValidationFilter;
import com.swp.project.filter.LoginRequestValidationFilter;
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
    private final LoginRequestValidationFilter loginRequestValidationFilter;
    private final CustomOAuth2UserService customOAuth2UserService;

    private static final String LOGIN_URL = "/login";

    private static final String HOME_URL = "/";

    private static final String[] PUBLIC_MATCHERS = {
            "/",
            "/register/**",
            "/login/**",
            "/css/**",
            "/js/**",
            "/images/**",
            "/forgot-password/**",
            "/verify-otp/**"
    };

    private static final String[] ADMIN_MATCHERS = {
            "/admin/**"
    };

    private static final  String[] MANAGER_MATCHERS = {
            "/manager/**"
    };

    private static final  String[] SELLER_MATCHERS = {
            "/seller/**"
    };

    private static final  String[] SHIPPER_MATCHERS = {
            "/shipper/**"
    };

    private static final String[] CUSTOMER_SUPPORT_MATCHERS = {
            "/customer-support/**",
    };

    // Thời gian remember-me (s)
    private static final int REMEMBER_ME_VALIDITY = Integer.MAX_VALUE;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(loginRequestValidationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(captchaValidationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(i -> i
                        .requestMatchers(PUBLIC_MATCHERS).permitAll()
                        .requestMatchers(ADMIN_MATCHERS).hasAuthority("Admin")
                        .requestMatchers(MANAGER_MATCHERS).hasAuthority("Manager")
                        .requestMatchers(SELLER_MATCHERS).hasAuthority("Seller")
                        .requestMatchers(SHIPPER_MATCHERS).hasAuthority("Shipper")
                        .requestMatchers(CUSTOMER_SUPPORT_MATCHERS).hasAuthority("Customer Support")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(i -> i
                                .accessDeniedPage("/")
                )
                .formLogin(i -> i
                        .loginPage(LOGIN_URL)
                        .usernameParameter("email")
                        .failureHandler(loginFailureHandler())
                        .defaultSuccessUrl(HOME_URL,true)
                )
                .oauth2Login(i -> i
                        .loginPage(LOGIN_URL)
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
