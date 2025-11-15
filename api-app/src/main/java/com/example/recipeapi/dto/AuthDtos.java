package com.example.recipeapi.dto;

import java.util.List;

public class AuthDtos {
    public static class SignupRequest {
        public String email;
        public String password;
        public String handle;
        public String displayName;
        public String role; // "chef" or "user"
    }
    public static class LoginRequest {
        public String email;
        public String password;
    }
    public static class LoginResponse {
        public String accessToken;
        public String refreshToken;
        public long accessTtlMs;
        public LoginResponse(String accessToken, String refreshToken, long accessTtlMs){
            this.accessToken = accessToken; this.refreshToken = refreshToken; this.accessTtlMs = accessTtlMs;
        }
    }
    public static class RefreshRequest {
        public String refreshToken;
    }
}
