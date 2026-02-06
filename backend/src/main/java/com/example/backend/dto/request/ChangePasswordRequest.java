package com.example.backend.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
public class ChangePasswordRequest {
    private Long userId;
    private String oldPassword;
    private String newPassword;
    private String confirmPassword; // 确认新密码
}