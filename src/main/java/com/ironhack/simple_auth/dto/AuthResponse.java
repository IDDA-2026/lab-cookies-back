package com.ironhack.simple_auth.dto;

/** Response shape for auth endpoints. The JWT lives in the httpOnly cookie. */
public record AuthResponse(UserDto user) {
}
