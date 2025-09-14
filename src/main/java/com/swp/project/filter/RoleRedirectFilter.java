package com.swp.project.filter;

import com.swp.project.entity.user.User;
import com.swp.project.security.SecurityUtils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public record RoleRedirectFilter(SecurityUtils securityUtils) implements Filter {

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser != null) {
            String role = currentUser.getAuthorities().stream().findFirst().get().getAuthority();
            if (role.equals("Admin") &&
                    !req.getRequestURI().startsWith("/admin")) {
                res.sendRedirect("/admin");
                return;
            }
            if (role.equals("Manager") &&
                    !req.getRequestURI().startsWith("/manager")) {
                res.sendRedirect("/manager");
                return;
            }
            if (role.equals("Seller") &&
                    !req.getRequestURI().startsWith("/seller")) {
                res.sendRedirect("/seller");
                return;
            }
            if (role.equals("Shipper") &&
                    !req.getRequestURI().startsWith("/shipper")) {
                res.sendRedirect("/shipper");
                return;
            }
            if (role.equals("Customer Support") &&
                    !req.getRequestURI().startsWith("/customer-support")) {
                res.sendRedirect("/customer-support");
                return;
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }
}
