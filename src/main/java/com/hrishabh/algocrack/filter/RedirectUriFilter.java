package com.hrishabh.algocrack.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * A lightweight filter that captures the ?redirect_uri= query parameter
 * from the OAuth2 login initiation request and stores it in a cookie.
 *
 * This allows the OAuth2AuthenticationSuccessHandler to redirect the user
 * back to their original page after login (e.g., /problem/10).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RedirectUriFilter extends OncePerRequestFilter {

    public static final String REDIRECT_URI_COOKIE = "redirect_uri";
    private static final int COOKIE_MAX_AGE = 180; // 3 minutes

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Only process OAuth2 authorization initiation requests
        if (request.getRequestURI().contains("/oauth2/authorization/")) {
            String redirectUri = request.getParameter("redirect_uri");
            if (redirectUri != null && !redirectUri.isEmpty()) {
                Cookie cookie = new Cookie(REDIRECT_URI_COOKIE, redirectUri);
                cookie.setPath("/");
                cookie.setHttpOnly(true);
                cookie.setMaxAge(COOKIE_MAX_AGE);
                response.addCookie(cookie);
            }
        }

        filterChain.doFilter(request, response);
    }
}
