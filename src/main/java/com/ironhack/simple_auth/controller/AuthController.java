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

import com.ironhack.simple_auth.dto.AuthResponse;
import com.ironhack.simple_auth.dto.LoginRequest;
import com.ironhack.simple_auth.dto.SignupRequest;
import com.ironhack.simple_auth.dto.UserDto;
import com.ironhack.simple_auth.model.User;
import com.ironhack.simple_auth.security.JwtTokenProvider;
import com.ironhack.simple_auth.service.AuthService;

import java.time.Duration;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /** Public: register a new user. The user still has to log in afterwards to get a cookie. */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
        User user = authService.signup(request);
        return ResponseEntity.ok(new AuthResponse(UserDto.from(user)));
    }

    /**
     * Public: validate credentials, then send the JWT back as an httpOnly
     * cookie (via ResponseCookie + the Set-Cookie header). The body only
     * carries the UserDto now.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        User user = authService.authenticate(request.email(), request.password());
        String jwt = jwtTokenProvider.createToken(user);

        ResponseCookie cookie = ResponseCookie.from("token", jwt)
                .httpOnly(true)
                .secure(false) // set true in production (requires https)
                .path("/")
                .maxAge(Duration.ofMillis(jwtTokenProvider.getExpirationMs()))
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(UserDto.from(user)));
    }

    /** Protected: returns the currently authenticated user. */
    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserDto.from(user));
    }

    /**
     * Protected: logout. Expires the httpOnly cookie immediately
     * (same name/path, maxAge 0) so the browser/Postman drops it.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie expiredCookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .build();
    }
}
