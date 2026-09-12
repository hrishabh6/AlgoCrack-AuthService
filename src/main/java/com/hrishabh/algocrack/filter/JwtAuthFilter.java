package com.hrishabh.algocrack.filter;

import com.hrishabh.algocrack.logging.LoggingConstants;
import com.hrishabh.algocrack.logging.StructuredLogger;
import com.hrishabh.algocrack.services.JwtService;
import com.hrishabh.algocrack.services.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final StructuredLogger structuredLogger = new StructuredLogger(JwtAuthFilter.class, "AuthService");

    @Autowired
    private UserDetailsServiceImpl userDetailService;


    private  final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("jwtToken")) {
                    token = cookie.getValue();
                }
            }
        }

        if (token == null) {
            structuredLogger.warn("JWT validation failed: token cookie missing",
                    LoggingConstants.EVENT_TYPE, LoggingConstants.EventType.AUTH,
                    LoggingConstants.TYPE, "Warn",
                    LoggingConstants.OPERATION, "jwt_filter",
                    LoggingConstants.STATUS, "FAILED",
                    LoggingConstants.HTTP_PATH, request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            String email = jwtService.getEmail(token);

            if (email != null) {

                UserDetails userDetails = userDetailService.loadUserByUsername(email);
                if (jwtService.validateToken(token, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, Collections.emptyList());

                    usernamePasswordAuthenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    structuredLogger.info("JWT validation succeeded",
                            LoggingConstants.EVENT_TYPE, LoggingConstants.EventType.AUTH,
                            LoggingConstants.OPERATION, "jwt_filter",
                            LoggingConstants.STATUS, "SUCCESS",
                            LoggingConstants.HTTP_PATH, request.getRequestURI());
                } else {
                    structuredLogger.warn("JWT validation failed",
                            LoggingConstants.EVENT_TYPE, LoggingConstants.EventType.AUTH,
                            LoggingConstants.TYPE, "Warn",
                            LoggingConstants.OPERATION, "jwt_filter",
                            LoggingConstants.STATUS, "FAILED",
                            LoggingConstants.HTTP_PATH, request.getRequestURI());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }
        } catch (Exception e) {
            structuredLogger.warn("JWT validation failed",
                    LoggingConstants.EVENT_TYPE, LoggingConstants.EventType.AUTH,
                    LoggingConstants.TYPE, "Warn",
                    LoggingConstants.OPERATION, "jwt_filter",
                    LoggingConstants.STATUS, "FAILED",
                    LoggingConstants.HTTP_PATH, request.getRequestURI(),
                    LoggingConstants.ERROR_CODE, e.getClass().getSimpleName());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }


    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        return !( "/api/v1/auth/validate".equals(path) && "GET".equalsIgnoreCase(method) );
    }



}
