package com.ironhack.simple_auth.dto;

/**
 * Today's response shape: the JWT travels in the BODY.
 *
 * In class this is exactly what we change: the token will move OUT of the body
 * and into an httpOnly cookie, leaving only the UserDto here.
 */
public record AuthResponse(UserDto user) {
}
