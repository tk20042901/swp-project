package com.swp.project.filter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CaptchaValidationFilter extends OncePerRequestFilter {

    @Value("${recaptcha.secret-key}")
    private String recaptchaSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();
        List<String> needToCheckCaptchaPaths = List.of(
                "/login",
                "/register",
                "/forgot-password"
        );
        if ("POST".equalsIgnoreCase(request.getMethod())
                && needToCheckCaptchaPaths.contains(servletPath)) {
            if (!verifyCaptcha(request.getParameter("g-recaptcha-response"))) {
                response.sendRedirect(servletPath + "?invalid_captcha");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean verifyCaptcha(String recaptchaResponse) {
        if(recaptchaResponse == null) {
            return false;
        }
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", recaptchaSecret);
        params.add("response", recaptchaResponse);
        var body = new RestTemplate().postForObject("https://www.google.com/recaptcha/api/siteverify", params, Map.class);
        return body != null && Boolean.TRUE.equals(body.get("success"));
    }
}
