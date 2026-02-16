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
import java.util.Optional;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final String REDIRECT_URI_COOKIE = "redirect_uri";
    private static final String DEFAULT_TARGET = "http://localhost:3000/oauth2/success";
    private static final String ALLOWED_ORIGIN = "http://localhost:3000";

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        String targetUrl = DEFAULT_TARGET;

        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
            String jwtToken = (String) oauthUser.getAttributes().get("jwt");

            // 1. Check for redirect_uri cookie (set by RedirectUriFilter)
            Optional<String> redirectUri = getCookieValue(request, REDIRECT_URI_COOKIE);
            if (redirectUri.isPresent() && redirectUri.get().startsWith(ALLOWED_ORIGIN)) {
                targetUrl = redirectUri.get();
            }

            // 2. Append token to target URL
            String separator = targetUrl.contains("?") ? "&" : "?";
            targetUrl += separator + "token=" + jwtToken;

            // 3. Clean up redirect_uri cookie
            deleteCookie(response, REDIRECT_URI_COOKIE);

            // 4. Delete old token cookie if exists
            deleteCookie(response, "leetoken");

            // 5. Create a new secure HttpOnly cookie for the JWT
            Cookie jwtCookie = new Cookie("leetoken", jwtToken);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
            response.addCookie(jwtCookie);

            // 6. Redirect
            response.sendRedirect(targetUrl);
        } else {
            response.sendRedirect(DEFAULT_TARGET + "?token=error");
        }
    }

    private Optional<String> getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return Optional.of(cookie.getValue());
                }
            }
        }
        return Optional.empty();
    }

    private void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
