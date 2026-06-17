package com.ironhack.simple_auth.dto;

 @Data
    public static class SignupRequest {
        private String email;
        private String password;
        private String name;
    }