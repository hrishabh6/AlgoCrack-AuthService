package com.hrishabh.algocrack.controller;

import com.hrishabh.algocrack.dto.AuthRequestDto;
import com.hrishabh.algocrack.dto.AuthResponseDto;
import com.hrishabh.algocrack.dto.UserDto;
import com.hrishabh.algocrack.dto.UserSignupRequestDto;
import com.hrishabh.algocrack.repository.UserRepository;
import com.hrishabh.algocrack.services.AuthService;
import com.hrishabh.algocrack.services.JwtService;
import com.hrishabh.algocrackentityservice.models.User;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

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

        UserDto response = authService.signUp(userSignupRequestDto);

        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }

    @GetMapping("/oauth2/success")
    public String test() {
        return "You hit backend!";
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signIn(@RequestBody AuthRequestDto authRequestDto, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequestDto.getEmail(), authRequestDto.getPassword()));

        if (authentication.isAuthenticated()) {
            // Look up user to get userId for JWT claims
            User user = userRepository.findByEmail(authRequestDto.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Build claims with userId and role for Gateway to forward as trusted headers
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getUserId());
            claims.put("role", "USER"); // TODO: use user.getRole() when role field is added

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
            return new ResponseEntity<>(AuthResponseDto.builder()
                    .success(true)
                    .token(jwtToken)
                    .build(), HttpStatus.OK);
        }
        return new ResponseEntity<>("Auth not successful", HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(HttpServletRequest request, HttpServletResponse response) {
        for (Cookie cookie : request.getCookies()) {
            System.out.println(cookie.getName() + " : " + cookie.getValue());
        }
        return ResponseEntity.ok("Success");
    }

}
