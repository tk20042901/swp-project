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

    private static final int MAX_LENGTH = 255;
    private static final Pattern EMAIL_REGEX = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/login".equals(path)) {
            String email = request.getParameter("email");
            String password = request.getParameter("password");

            if (!isValidEmail(email) || !isValidPassword(password)) {
                response.sendRedirect("/login?invalid_input");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isValidEmail(String email) {
        if (email == null) return false;
        String e = email.trim();
        if (e.isEmpty() || e.length() > MAX_LENGTH) return false;
        return EMAIL_REGEX.matcher(e).matches();
    }

    private boolean isValidPassword(String password) {
        if (password == null) return false;
        String p = password.trim();
        return !p.isEmpty() && p.length() <= MAX_LENGTH;
    }
}

