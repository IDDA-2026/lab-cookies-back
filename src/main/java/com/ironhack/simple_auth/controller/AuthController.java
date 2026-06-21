package com.ironhack.simple_auth.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ironhack.simple_auth.dto.LoginRequest;
import com.ironhack.simple_auth.dto.SignupRequest;
import com.ironhack.simple_auth.dto.UserDto;
import com.ironhack.simple_auth.model.User;
import com.ironhack.simple_auth.security.JwtTokenProvider;
import com.ironhack.simple_auth.service.AuthService;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /** Public: register a new user */
    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@RequestBody SignupRequest request) {
        User user = authService.signup(request);
        String jwt = jwtTokenProvider.createToken(user);

        ResponseCookie cookie = ResponseCookie.from("token", jwt)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(UserDto.from(user));
    }

    /** Public: validate credentials and return a JWT via httpOnly cookie */
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody LoginRequest request) {
        User user = authService.authenticate(request.email(), request.password());
        String jwt = jwtTokenProvider.createToken(user);

        ResponseCookie cookie = ResponseCookie.from("token", jwt)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(UserDto.from(user));
    }

    /** Protected: returns the currently authenticated user. */
    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserDto.from(user));
    }

    /** Protected: logout and expire the cookie */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}