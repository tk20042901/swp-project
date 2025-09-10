package com.swp.project.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class CaptchaValidationFilter extends OncePerRequestFilter {

    @Value("${recaptcha.secret-key}")
    private String recaptchaSecret;

    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        boolean isPostMethod = "POST".equalsIgnoreCase(request.getMethod());
        String captchaResponse = request.getParameter("g-recaptcha-response");
        boolean isVerifiedCaptcha = captchaResponse != null && verifyCaptcha(captchaResponse);
        String servletPath = request.getServletPath();
        String[] needToCheckCaptchaPaths = {
                "/login",
                "/register",
                "/forgot-password"
        };
        for(String path : needToCheckCaptchaPaths)
            if (path.equals(servletPath) && isPostMethod)
                if (!isVerifiedCaptcha) {
                    response.sendRedirect(path + "?invalid_captcha");
                    return;
                }
        filterChain.doFilter(request, response);
    }

    private boolean verifyCaptcha(String responseToken) {
        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", recaptchaSecret);
        params.add("response", responseToken);

        var body = restTemplate.postForObject(VERIFY_URL, params, Map.class);
        return body != null && Boolean.TRUE.equals(body.get("success"));
    }
}
