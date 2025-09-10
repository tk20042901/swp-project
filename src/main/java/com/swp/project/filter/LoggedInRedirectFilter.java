package com.swp.project.filter;

import com.swp.project.security.SecurityUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public record LoggedInRedirectFilter(SecurityUtils securityUtils) implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) servletResponse;
        if (securityUtils.getCurrentUser() != null) {
            res.sendRedirect("/home");
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
