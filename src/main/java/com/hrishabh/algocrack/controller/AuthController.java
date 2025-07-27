package com.hrishabh.algocrack.controller;


import com.hrishabh.algocrack.dto.AuthRequestDto;
import com.hrishabh.algocrack.dto.AuthResponseDto;
import com.hrishabh.algocrack.dto.UserDto;
import com.hrishabh.algocrack.dto.UserSignupRequestDto;
import com.hrishabh.algocrack.services.AuthService;
import com.hrishabh.algocrack.services.JwtService;
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



@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${cookie.expiry}")
    private int cookieExpiry;

    public AuthController(AuthService authService, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
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
        System.out.println("Request comming : "  + authRequestDto.getEmail() + " " + authRequestDto.getPassword());
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequestDto.getEmail(), authRequestDto.getPassword()));

        System.out.println(authentication.isAuthenticated());
        if (authentication.isAuthenticated()) {
            String jwtToken = jwtService.createToken(authRequestDto.getEmail());

            ResponseCookie cookie = ResponseCookie.from("jwtToken", jwtToken).httpOnly(true).secure(false).maxAge(3600).build();
            response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            return new ResponseEntity<>(AuthResponseDto.builder().success(true).build(), HttpStatus.OK);

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
