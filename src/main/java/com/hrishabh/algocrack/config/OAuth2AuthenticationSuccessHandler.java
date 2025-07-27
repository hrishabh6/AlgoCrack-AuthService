package com.hrishabh.algocrack.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
            String jwtToken = (String) oauthUser.getAttributes().get("jwt");

            // 🧹 Step 1: Delete old token cookie if exists
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("leetoken".equals(cookie.getName())) {
                        cookie.setValue(null);
                        cookie.setPath("/");
                        cookie.setMaxAge(0);
                        response.addCookie(cookie);
                    }
                }
            }

            // ✅ Step 2: Create a new secure HttpOnly cookie
            Cookie jwtCookie = new Cookie("leetoken", jwtToken);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(true); // true for HTTPS in production
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days

            response.addCookie(jwtCookie);

            // 🔁 Step 3: Redirect without token in URL
            response.sendRedirect("http://localhost:3000/oauth2/success");
        } else {
            response.sendRedirect("http://localhost:3000/oauth2/success?token=error");
        }
    }
}
