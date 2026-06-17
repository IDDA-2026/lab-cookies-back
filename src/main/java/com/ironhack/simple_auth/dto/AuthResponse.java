package com.ironhack.simple_auth.dto;

/** Response shape for auth endpoints. The JWT travels in an httpOnly cookie. */
public record AuthResponse(UserDto user) {
}
