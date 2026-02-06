package com.example.backend.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {
    private String username;
    private String password;
    private Boolean rememberMe = false; // 记住我选项
}

