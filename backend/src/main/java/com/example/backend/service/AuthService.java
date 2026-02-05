package com.example.backend.service;

import com.example.backend.po.User;
import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
    void logout(String token);
    User getCurrentUser(String token);
    String generateToken(User user);
    boolean validateToken(String token);
}