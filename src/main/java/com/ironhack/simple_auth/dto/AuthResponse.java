package com.ironhack.simple_auth.dto;

/**
 * Auth responses carry only the user — the JWT travels in an httpOnly cookie.
 */
public record AuthResponse(UserDto user) {
}
