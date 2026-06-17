package com.ironhack.simple_auth.controller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

@RestController
@RequestMapping("/api")
public class AuthController {

    private static final String TOKEN_COOKIE_NAME = "token";
    private static final Duration TOKEN_COOKIE_MAX_AGE = Duration.ofHours(1);

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /** Public: register a new user. */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
        User user = authService.signup(request);
        return ResponseEntity.ok(new AuthResponse(UserDto.from(user)));
    }

    /** Public: validate credentials and send a JWT inside an httpOnly cookie. */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        User user = authService.authenticate(request.email(), request.password());
        String jwt = jwtTokenProvider.createToken(user);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, tokenCookie(jwt).toString())
                .body(new AuthResponse(UserDto.from(user)));
    }

    /** Protected: returns the currently authenticated user. */
    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserDto.from(user));
    }

    /** Protected: logout by expiring the token cookie. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearTokenCookie().toString())
                .build();
    }

    private ResponseCookie tokenCookie(String token) {
        return ResponseCookie.from(TOKEN_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(TOKEN_COOKIE_MAX_AGE)
                .sameSite("Lax")
                .build();
    }

    private ResponseCookie clearTokenCookie() {
        return ResponseCookie.from(TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ZERO)
                .sameSite("Lax")
                .build();
    }
}
