package com.ironhack.simple_auth.dto;

import com.ironhack.simple_auth.model.User;

@Data
    public static class UserResponse {
        private Long id;
        private String email;
        private String name;
 
        public UserResponse(Long id, String email, String name) {
            this.id = id;
            this.email = email;
            this.name = name;
        }
    }
