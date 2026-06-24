package com.ironhack.simple_auth.security;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.Cookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ironhack.simple_auth.model.User;
import com.ironhack.simple_auth.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.WebUtils;

/**
 * Runs once per request. It looks for a JWT, validates it, and if valid tells
 * Spring Security "this request is authenticated as that user".
 *
 * TODAY it reads the token from the "Authorization: Bearer ..." header.
 * IN CLASS we change ONLY the "where do we read the token from" part:
 * instead of the header, we'll read it from the httpOnly cookie named "token".
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && tokenProvider.validateToken(token)) {
            String email = tokenProvider.getEmail(token);

            userRepository.findByEmail(email).ifPresent(user -> {
                var auth = new UsernamePasswordAuthenticationToken(
                        user, null, authorities(user));
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }

        filterChain.doFilter(request, response);
    }

    /** Pull the token out of the "Authorization: Bearer <token>" header. */
    private String resolveToken(HttpServletRequest request) {

        Cookie cookie = WebUtils.getCookie(request, "token");
        // The header is no longer used, the token doesn't go in the headers.
        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }

    private List<SimpleGrantedAuthority> authorities(User user) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }
}
