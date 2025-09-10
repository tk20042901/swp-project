package com.swp.project.config;

import com.swp.project.filter.LoggedInRedirectFilter;
import com.swp.project.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class FilterConfig {

    private final SecurityUtils securityUtils;

    @Bean
    public FilterRegistrationBean<LoggedInRedirectFilter> loggedInRedirectFilter() {
        FilterRegistrationBean<LoggedInRedirectFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new LoggedInRedirectFilter(securityUtils));
        registrationBean.addUrlPatterns("/login","/register","/forgot-password");
        registrationBean.setOrder(0);
        return registrationBean;
    }
}
