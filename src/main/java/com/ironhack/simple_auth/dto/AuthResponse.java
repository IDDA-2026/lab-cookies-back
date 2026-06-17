package com.ironhack.simple_auth.dto;

@Data
    public static class LoginResponse {
        private String token;
        private String email;
        private String name;
 
        public LoginResponse(String token, String email, String name) {
            this.token = token;
            this.email = email;
            this.name = name;
        }