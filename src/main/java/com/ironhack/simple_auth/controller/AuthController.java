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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /** Public: register a new user, then hand back a token (in the body, for now). */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
        User user = authService.signup(request);
        String jwt = jwtTokenProvider.createToken(user);
        return ResponseEntity.ok(new AuthResponse(UserDto.from(user), jwt));
    }

    /**
     * Public: validate credentials and return a JWT in an httpOnly cookie.
     * The response body carries only the UserDto.
     */
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody LoginRequest request, jakarta.servlet.http.HttpServletResponse response) {
        User user = authService.authenticate(request.email(), request.password());
        String jwt = jwtTokenProvider.createToken(user);
        
        ResponseCookie cookie = ResponseCookie.from("token", jwt)
                .httpOnly(true)
                .path("/")
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        
        return ResponseEntity.ok(UserDto.from(user));
    }

    /** Protected: returns the currently authenticated user. */
    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(UserDto.from(user));
    }

    /**
     * Protected: logout.
     * Clear the httpOnly cookie by setting maxAge(0).
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(jakarta.servlet.http.HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        
        return ResponseEntity.ok().build();
    }
}
