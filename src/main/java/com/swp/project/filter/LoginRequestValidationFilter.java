package com.swp.project.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

@Component
public class LoginRequestValidationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/login".equals(request.getServletPath())) {
            if (!isValidEmail(request.getParameter("email")) ||
                    !isValidPassword(request.getParameter("password"))) {
                response.sendRedirect("/login?invalid_input");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isValidEmail(String email) {
        if (email == null) return false;
        if (email.isEmpty() || email.length() > 255) return false;
        return Pattern.compile(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        ).matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        if (password == null) return false;
        return !password.isEmpty() && password.length() <= 255;
    }
}

