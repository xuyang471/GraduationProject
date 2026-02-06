package com.example.backend.service;

import com.example.backend.po.User;
import com.example.backend.dao.UserDao;
import com.example.backend.dto.request.LoginRequest;
import com.example.backend.dto.request.ChangePasswordRequest;
import com.example.backend.dto.response.LoginResponse;
import com.example.backend.service.AuthService;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDao userDao;

    // 简单的token存储，实际应该使用Redis
    private Map<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        // 验证输入
        if (loginRequest == null ||
                !StringUtils.hasText(loginRequest.getUsername()) ||
                !StringUtils.hasText(loginRequest.getPassword())) {
            throw new RuntimeException("用户名和密码不能为空");
        }

        String username = loginRequest.getUsername().trim();
        String password = loginRequest.getPassword();

        try {
            // 用户认证
            User user = userService.authenticate(username, password);

            // 检查是否需要修改密码
            boolean requirePasswordChange = isFirstLogin(username, password);

            // 生成token
            String token = generateToken(user);

            // 保存token信息
            TokenInfo tokenInfo = new TokenInfo();
            tokenInfo.setUserId(user.getUserId());
            tokenInfo.setUsername(user.getUsername());
            tokenInfo.setGeneratedAt(LocalDateTime.now());
            tokenInfo.setExpiresAt(LocalDateTime.now().plusHours(24));
            tokenStore.put(token, tokenInfo);

            // 构建响应
            LoginResponse response = new LoginResponse();
            response.setSuccess(true);
            response.setMessage("登录成功");
            response.setToken(token);
            response.setUser(user);
            response.setRequirePasswordChange(requirePasswordChange);
            response.setExpiresIn(24 * 60 * 60); // 24小时

            // 设置额外信息
            Map<String, Object> additionalInfo = new HashMap<>();
            additionalInfo.put("roleName", getRoleName(user.getRole()));
            additionalInfo.put("loginTime", LocalDateTime.now());
            additionalInfo.put("rememberMe", loginRequest.getRememberMe());
            response.setAdditionalInfo(additionalInfo);

            return response;

        } catch (RuntimeException e) {
            LoginResponse response = new LoginResponse();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return response;
        }
    }

    @Override
    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            tokenStore.remove(token);
        }
    }

    @Override
    public User getCurrentUser(String token) {
        if (!validateToken(token)) {
            throw new RuntimeException("Token无效或已过期");
        }

        TokenInfo tokenInfo = tokenStore.get(token);
        if (tokenInfo == null) {
            throw new RuntimeException("Token信息不存在");
        }

        return userService.getUserById(tokenInfo.getUserId());
    }

    @Override
    @Transactional
    public User changePassword(ChangePasswordRequest request) {
        // 验证输入
        if (request == null) {
            throw new RuntimeException("请求参数不能为空");
        }

        if (request.getUserId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        if (!StringUtils.hasText(request.getOldPassword())) {
            throw new RuntimeException("原密码不能为空");
        }

        if (!StringUtils.hasText(request.getNewPassword())) {
            throw new RuntimeException("新密码不能为空");
        }

        if (request.getNewPassword().length() < 6) {
            throw new RuntimeException("新密码至少需要6位");
        }

        // 验证新密码和确认密码是否一致
        if (StringUtils.hasText(request.getConfirmPassword()) &&
                !request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("新密码和确认密码不一致");
        }

        // 修改密码
        return userService.changePassword(
                request.getUserId(),
                request.getOldPassword(),
                request.getNewPassword()
        );
    }

    @Override
    public boolean isFirstLogin(String username, String password) {
        // 检查是否使用默认密码（账号后6位）
        String defaultPassword = username.length() >= 6 ?
                username.substring(username.length() - 6) : username;

        return password.equals(defaultPassword);
    }

    @Override
    public String generateToken(User user) {
        // 简单的token生成，实际应该使用JWT
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = String.valueOf(new Random().nextInt(100000));

        return "token-" + user.getUserId() + "-" + timestamp + "-" + random;
    }

    @Override
    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        TokenInfo tokenInfo = tokenStore.get(token);
        if (tokenInfo == null) {
            return false;
        }

        // 检查token是否过期
        if (tokenInfo.getExpiresAt().isBefore(LocalDateTime.now())) {
            tokenStore.remove(token); // 移除过期token
            return false;
        }

        return true;
    }

    @Override
    public Long extractUserIdFromToken(String token) {
        if (!validateToken(token)) {
            return null;
        }

        TokenInfo tokenInfo = tokenStore.get(token);
        return tokenInfo != null ? tokenInfo.getUserId() : null;
    }

    @Override
    public String refreshToken(String oldToken) {
        if (!validateToken(oldToken)) {
            throw new RuntimeException("原Token无效或已过期");
        }

        TokenInfo oldTokenInfo = tokenStore.remove(oldToken);
        if (oldTokenInfo == null) {
            throw new RuntimeException("Token信息不存在");
        }

        User user = userService.getUserById(oldTokenInfo.getUserId());
        String newToken = generateToken(user);

        // 更新token信息
        TokenInfo newTokenInfo = new TokenInfo();
        newTokenInfo.setUserId(user.getUserId());
        newTokenInfo.setUsername(user.getUsername());
        newTokenInfo.setGeneratedAt(LocalDateTime.now());
        newTokenInfo.setExpiresAt(LocalDateTime.now().plusHours(24));

        tokenStore.put(newToken, newTokenInfo);

        return newToken;
    }

    @Override
    public void forceLogout(Long userId) {
        // 移除所有该用户的token
        tokenStore.entrySet().removeIf(entry ->
                entry.getValue().getUserId().equals(userId)
        );
    }

    @Override
    public List<String> getUserPermissions(String token) {
        if (!validateToken(token)) {
            return Collections.emptyList();
        }

        Long userId = extractUserIdFromToken(token);
        if (userId == null) {
            return Collections.emptyList();
        }

        User user = userService.getUserById(userId);
        return getUserPermissionsByRole(user.getRole());
    }

    @Override
    public boolean hasPermission(String token, String permission) {
        List<String> permissions = getUserPermissions(token);
        return permissions.contains(permission);
    }

    // 私有辅助方法
    private String getRoleName(Integer role) {
        if (role == null) return "未知";
        switch (role) {
            case 1: return "学生";
            case 2: return "教职工";
            case 3: return "管理员";
            default: return "未知";
        }
    }

    private List<String> getUserPermissionsByRole(Integer role) {
        List<String> permissions = new ArrayList<>();

        if (role == null) {
            return permissions;
        }

        // 学生权限
        if (role == 1) {
            permissions.add("user:view");
            permissions.add("user:edit");
            permissions.add("lost:search");
            permissions.add("lost:claim");
            permissions.add("claim:create");
            permissions.add("complaint:create");
        }

        // 教职工权限（拥有学生所有权限，外加一些特殊权限）
        if (role == 2) {
            permissions.addAll(getUserPermissionsByRole(1)); // 包含学生权限
            permissions.add("lost:report");
            permissions.add("item:manage");
        }

        // 管理员权限
        if (role == 3) {
            permissions.add("*:*"); // 所有权限
        }

        return permissions;
    }

    // Token信息内部类
    private static class TokenInfo {
        private Long userId;
        private String username;
        private LocalDateTime generatedAt;
        private LocalDateTime expiresAt;

        // getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    }
}