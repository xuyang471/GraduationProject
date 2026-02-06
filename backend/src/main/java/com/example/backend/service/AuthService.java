package com.example.backend.service;

import com.example.backend.po.User;
import com.example.backend.dto.request.LoginRequest;
import com.example.backend.dto.response.LoginResponse;
import com.example.backend.dto.request.ChangePasswordRequest;

import java.util.List;

public interface AuthService {

    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 用户登出
     * @param token 用户token
     */
    void logout(String token);

    /**
     * 获取当前用户信息
     * @param token 用户token
     * @return 用户信息
     */
    User getCurrentUser(String token);

    /**
     * 修改密码
     * @param request 修改密码请求
     * @return 修改后的用户信息
     */
    User changePassword(ChangePasswordRequest request);

    /**
     * 验证用户是否首次登录（使用默认密码）
     * @param username 用户名
     * @param password 密码
     * @return 是否需要修改密码
     */
    boolean isFirstLogin(String username, String password);

    /**
     * 生成用户token
     * @param user 用户信息
     * @return token字符串
     */
    String generateToken(User user);

    /**
     * 验证token有效性
     * @param token 用户token
     * @return 是否有效
     */
    boolean validateToken(String token);

    /**
     * 从token中提取用户ID
     * @param token 用户token
     * @return 用户ID
     */
    Long extractUserIdFromToken(String token);

    /**
     * 刷新token
     * @param oldToken 旧token
     * @return 新token
     */
    String refreshToken(String oldToken);

    /**
     * 强制下线用户
     * @param userId 用户ID
     */
    void forceLogout(Long userId);

    /**
     * 获取用户权限信息
     * @param token 用户token
     * @return 权限列表
     */
    List<String> getUserPermissions(String token);

    /**
     * 验证用户是否有权限
     * @param token 用户token
     * @param permission 权限标识
     * @return 是否有权限
     */
    boolean hasPermission(String token, String permission);
}