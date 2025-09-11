package com.swp.project.filter;

import com.swp.project.security.CustomUserDetails;
import com.swp.project.security.SecurityUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public record RoleRedirectFilter(SecurityUtils securityUtils) implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;
        CustomUserDetails currentUser = securityUtils.getCurrentUser();
        if (currentUser != null) {
            if (currentUser
                    .getAuthorities()
                    .stream()
                    .anyMatch(a -> a
                            .getAuthority().equals("Admin")) &&
                    !req.getRequestURI().startsWith("/admin")) {
                res.sendRedirect("/admin/dashboard");
                return;
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
