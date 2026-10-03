package com.hrishabh.algocrack.controller;

import com.hrishabh.algocrack.dto.AuthRequestDto;
import com.hrishabh.algocrack.dto.AuthResponseDto;
import com.hrishabh.algocrack.dto.UserDto;
import com.hrishabh.algocrack.dto.UserInfoDto;
import com.hrishabh.algocrack.dto.UserSignupRequestDto;
import com.hrishabh.algocrack.logging.LoggingConstants;
import com.hrishabh.algocrack.logging.StructuredLogger;
import com.hrishabh.algocrack.repository.UserRepository;
import com.hrishabh.algocrack.services.AuthService;
import com.hrishabh.algocrack.services.JwtService;
import com.hrishabh.algocrack.models.User;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final StructuredLogger structuredLogger = new StructuredLogger(AuthController.class, "AuthService");

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${cookie.expiry}")
    private int cookieExpiry;

    public AuthController(AuthService authService, AuthenticationManager authenticationManager,
            JwtService jwtService, UserRepository userRepository) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@RequestBody UserSignupRequestDto userSignupRequestDto) {
        structuredLogger.info("Signup request received",
                LoggingConstants.EVENT_TYPE, LoggingConstants.EventType.AUTH,
                LoggingConstants.OPERATION, "signup",
                LoggingConstants.USER_ID, userSignupRequestDto.getUser_id(),
                "email_present", userSignupRequestDto.getEmail() != null);

        UserDto response = authService.signUp(userSignupRequestDto);

        structuredLogger.info("Signup request completed",
                LoggingConstants.EVENT_TYPE, LoggingConstants.EventType.AUTH,
                LoggingConstants.OPERATION, "signup",
                LoggingConstants.USER_ID, response.getUser_id(),
                LoggingConstants.STATUS, "SUCCESS");

        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }

    @GetMapping("/oauth2/success")
    public String test() {
        return "You hit backend!";
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody AuthRequestDto authRequestDto, HttpServletResponse response) {
        structuredLogger.info("Signin request received",
                LoggingConstants.EVENT_TYPE, LoggingConstants.EventType.AUTH,
                LoggingConstants.OPERATION, "signin",
                "email_present", authRequestDto.getEmail() != null);

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequestDto.getEmail(), authRequestDto.getPassword()));
        } catch (AuthenticationException e) {
            structuredLogger.warn("Signin failed",
                    LoggingConstants.EVENT_TYPE, LoggingConstants.EventType.AUTH,
                    LoggingConstants.TYPE, "Warn",
                    LoggingConstants.OPERATION, "signin",
                    LoggingConstants.STATUS, "FAILED",
                    LoggingConstants.ERROR_CODE, e.getClass().getSimpleName());
            throw e;
        }

        if (authentication.isAuthenticated()) {
            // Look up user to get userId for JWT claims
            User user = userRepository.findByEmail(authRequestDto.getEmail())
                    .orElseThrow(() -> {
                        structuredLogger.error("Signin user lookup failed",
                                LoggingConstants.EVENT_TYPE, LoggingConstants.EventType.AUTH,
                                LoggingConstants.TYPE, "Error",
                                LoggingConstants.OPERATION, "signin",
                                LoggingConstants.STATUS, "FAILED");
                        return new RuntimeException("User not found");
                    });

            // Build claims with userId and role for Gateway to forward as trusted headers
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getUserId());
            claims.put("role", user.getRole().name());

            String jwtToken = jwtService.createToken(claims, authRequestDto.getEmail());

            // Set cookie (backward compatibility)
            ResponseCookie cookie = ResponseCookie.from("jwtToken", jwtToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(cookieExpiry)
                    .build();
            response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // Return token in response body too (for Authorization: Bearer header usage)
            structuredLogger.info("Signin completed",
                    LoggingConstants.EVENT_TYPE, LoggingConstants.EventType.AUTH,
                    LoggingConstants.OPERATION, "signin",
                    LoggingConstants.USER_ID, user.getUserId(),
                    LoggingConstants.STATUS, "SUCCESS",
                    "token_issued", true);

            return new ResponseEntity<>(AuthResponseDto.builder()
                    .success(true)
                    .token(jwtToken)
                    .build(), HttpStatus.OK);
        }
        structuredLogger.warn("Signin not authenticated",
                LoggingConstants.EVENT_TYPE, LoggingConstants.EventType.AUTH,
                LoggingConstants.TYPE, "Warn",
                LoggingConstants.OPERATION, "signin",
                LoggingConstants.STATUS, "FAILED");
        return new ResponseEntity<>("Auth not successful", HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(HttpServletRequest request, HttpServletResponse response) {
        int cookieCount = request.getCookies() != null ? request.getCookies().length : 0;
        structuredLogger.info("Token validation endpoint reached",
                LoggingConstants.EVENT_TYPE, LoggingConstants.EventType.AUTH,
                LoggingConstants.OPERATION, "validate",
                LoggingConstants.STATUS, "SUCCESS",
                "cookie_count", cookieCount);
        return ResponseEntity.ok("Success");
    }

    // ── Inter-Service APIs (Phase 7) ──────────────────────────────────

    /**
     * Get user details by userId.
     * Called by ProblemService's UserProfileService.
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserInfoDto> getUserByUserId(@PathVariable String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    structuredLogger.warn("User lookup failed",
                            LoggingConstants.EVENT_TYPE, LoggingConstants.EventType.AUTH,
                            LoggingConstants.TYPE, "Warn",
                            LoggingConstants.OPERATION, "user_lookup",
                            LoggingConstants.USER_ID, userId,
                            LoggingConstants.STATUS, "NOT_FOUND");
                    return new RuntimeException("User not found: " + userId);
                });
        structuredLogger.debug("User lookup completed",
                LoggingConstants.EVENT_TYPE, LoggingConstants.EventType.AUTH,
                LoggingConstants.OPERATION, "user_lookup",
                LoggingConstants.USER_ID, userId,
                LoggingConstants.STATUS, "SUCCESS");
        return ResponseEntity.ok(UserInfoDto.fromEntity(user));
    }

    /**
     * Check if a user exists by userId.
     * Called by ProblemService's UserProfileService (heatmap validation).
     */
    @GetMapping("/users/{userId}/exists")
    public ResponseEntity<Boolean> userExists(@PathVariable String userId) {
        boolean exists = userRepository.existsByUserId(userId);
        structuredLogger.debug("User existence check completed",
                LoggingConstants.EVENT_TYPE, LoggingConstants.EventType.AUTH,
                LoggingConstants.OPERATION, "user_exists",
                LoggingConstants.USER_ID, userId,
                LoggingConstants.STATUS, exists ? "FOUND" : "NOT_FOUND");
        return ResponseEntity.ok(exists);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationFailure(AuthenticationException e) {
        structuredLogger.warn("Authentication request failed",
                LoggingConstants.EVENT_TYPE, LoggingConstants.EventType.AUTH,
                LoggingConstants.TYPE, "Warn",
                LoggingConstants.OPERATION, "authenticate",
                LoggingConstants.STATUS, "FAILED",
                LoggingConstants.ERROR_CODE, e.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Auth not successful");
    }

}
