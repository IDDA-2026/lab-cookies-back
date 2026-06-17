package com.ironhack.simple_auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ironhack.simple_auth.dto.AuthResponse;
import com.ironhack.simple_auth.dto.LoginRequest;
import com.ironhack.simple_auth.dto.SignupRequest;
import com.ironhack.simple_auth.dto.UserDto;
import com.ironhack.simple_auth.model.User;
import com.ironhack.simple_auth.security.JwtTokenProvider;
import com.ironhack.simple_auth.service.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
        User user = authService.signup(request);
        String jwt = jwtTokenProvider.createToken(user);
        return ResponseEntity.ok(new AuthResponse(UserDto.from(user), jwt));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        User user = authService.authenticate(request.email(), request.password());
        String jwt = jwtTokenProvider.createToken(user);

        // HttpOnly Cookie 
        Cookie cookie = new Cookie("token", jwt);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); 
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 1day

        response.addCookie(cookie);

        // we dont have tokens insdide body ,instead we have null
        return ResponseEntity.ok(new AuthResponse(UserDto.from(user), null));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserDto.from(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        // Mövcud cookie-ni silmək üçün maxAge dəyərini 0 təyin edirik
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0); 

        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }
}