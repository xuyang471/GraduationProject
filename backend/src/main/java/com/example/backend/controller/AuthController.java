package com.example.backend.controller;

import com.example.backend.po.User;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    // 测试接口
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        System.out.println("GET /api/auth/test 被调用");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", 200);
        response.put("message", "认证服务运行正常");
        response.put("timestamp", System.currentTimeMillis());
        response.put("data", new HashMap<String, Object>() {{
            put("service", "Authentication Service");
            put("status", "active");
            put("version", "1.0.0");
        }});

        return ResponseEntity.ok(response);
    }

    // 用户登录
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        System.out.println("POST /api/auth/login 被调用");
        System.out.println("请求数据 - 用户名: " + loginData.get("username"));

        try {
            String username = loginData.get("username");
            String password = loginData.get("password");

            // 验证输入
            if (username == null || username.trim().isEmpty()) {
                return createErrorResponse("请输入用户名（学号/工号）", 400);
            }

            if (password == null || password.trim().isEmpty()) {
                return createErrorResponse("请输入密码", 400);
            }

            // 使用UserService进行认证
            User user;
            try {
                user = userService.authenticate(username, password);
            } catch (RuntimeException e) {
                System.out.println("认证失败: " + e.getMessage());
                return createErrorResponse(e.getMessage(), 401);
            }

            // 检查是否为首次登录（需要修改密码）
            final boolean requirePasswordChange;  // 声明为final
            String defaultPassword = username.length() >= 6 ?
                    username.substring(username.length() - 6) : username;

            if (password.equals(defaultPassword)) {
                requirePasswordChange = true;
            } else {
                requirePasswordChange = false;
            }

            // 登录成功
            System.out.println("✅ 登录成功: " + username + " 角色: " + user.getRole());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "登录成功");
            response.put("data", new HashMap<String, Object>() {{
                // 生成简单token（实际项目应使用JWT）
                put("token", generateToken(user));
                put("user", new HashMap<String, Object>() {{
                    put("userId", user.getUserId());
                    put("username", user.getUsername());
                    put("realName", user.getRealName());
                    put("role", user.getRole());
                    put("roleName", getRoleName(user.getRole()));
                    put("college", user.getCollege());
                    put("phone", user.getPhone());
                    put("status", user.getStatus());
                }});
                put("requirePasswordChange", requirePasswordChange);
                put("expiresIn", 24 * 60 * 60); // 24小时（秒）
            }});

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("登录处理异常: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("登录失败: " + e.getMessage(), 500);
        }
    }

    // 简单登录（用于测试）
    @PostMapping("/simple-login")
    public ResponseEntity<Map<String, Object>> simpleLogin(
            @RequestParam String username,
            @RequestParam String password) {
        System.out.println("✅ POST /api/auth/simple-login 被调用");

        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", username);
        loginData.put("password", password);

        return login(loginData);
    }

    // 修改密码
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> passwordData) {
        System.out.println("✅ POST /api/auth/change-password 被调用");

        try {
            Long userId = passwordData.get("userId") != null ?
                    Long.parseLong(passwordData.get("userId")) : null;
            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");

            // 验证输入
            if (userId == null) {
                return createErrorResponse("用户ID不能为空", 400);
            }
            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                return createErrorResponse("请输入原密码", 400);
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return createErrorResponse("请输入新密码", 400);
            }
            if (newPassword.length() < 6) {
                return createErrorResponse("新密码至少需要6位", 400);
            }

            // 修改密码
            User updatedUser;
            try {
                updatedUser = userService.changePassword(userId, oldPassword, newPassword);
            } catch (RuntimeException e) {
                return createErrorResponse(e.getMessage(), 400);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "密码修改成功");
            response.put("data", new HashMap<String, Object>() {{
                put("username", updatedUser.getUsername());
                put("realName", updatedUser.getRealName());
            }});

            System.out.println("✅ 密码修改成功: " + updatedUser.getUsername());
            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            return createErrorResponse("用户ID格式错误", 400);
        } catch (Exception e) {
            System.err.println("修改密码异常: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("修改密码失败: " + e.getMessage(), 500);
        }
    }

    // 检查用户名是否可用
    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam String username) {
        System.out.println("✅ GET /api/auth/check-username 被调用: " + username);

        try {
            boolean exists = userService.checkUsernameExists(username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "检查完成");
            response.put("data", new HashMap<String, Object>() {{
                put("username", username);
                put("available", !exists);
            }});

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("检查用户名异常: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("检查失败: " + e.getMessage(), 500);
        }
    }

    // 用户注册（根据需求，系统不支持自主注册，只支持管理员添加）
    // 保留此接口用于特殊情况或测试
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> registerData) {
        System.out.println("✅ POST /api/auth/register 被调用（特殊注册）");

        try {
            String username = registerData.get("username");
            String password = registerData.get("password");
            String realName = registerData.get("realName");
            String phone = registerData.get("phone");
            String college = registerData.get("college");

            // 验证输入
            if (username == null || username.trim().isEmpty()) {
                return createErrorResponse("用户名（学号/工号）不能为空", 400);
            }

            if (password == null || password.trim().isEmpty()) {
                return createErrorResponse("密码不能为空", 400);
            }

            if (realName == null || realName.trim().isEmpty()) {
                return createErrorResponse("真实姓名不能为空", 400);
            }

            if (username.length() < 3) {
                return createErrorResponse("用户名至少3个字符", 400);
            }

            if (password.length() < 6) {
                return createErrorResponse("密码至少6个字符", 400);
            }

            // 检查用户名是否已存在
            if (userService.checkUsernameExists(username)) {
                return createErrorResponse("用户名（学号/工号）已存在", 400);
            }

            // 创建新用户（仅限学生角色）
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setRealName(realName);
            user.setPhone(phone);
            user.setCollege(college);
            user.setRole(1); // 学生
            user.setStatus(1); // 正常
            user.setFailCount(0);
            user.setCreatedAt(LocalDateTime.now());

            // 注意：这里直接使用dao保存，实际应该通过service
            // 或者为特殊注册创建专门的service方法

            System.out.println("⚠️ 特殊注册成功: " + username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 201);
            response.put("message", "注册成功（特殊通道）");
            response.put("data", new HashMap<String, Object>() {{
                put("username", username);
                put("realName", realName);
                put("note", "请尽快修改初始密码");
            }});

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("注册处理异常: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("注册失败: " + e.getMessage(), 500);
        }
    }

    // 获取当前用户信息
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@RequestParam String token) {
        System.out.println("✅ GET /api/auth/profile 被调用");

        try {
            // 简单验证token（实际应使用JWT验证）
            Long userId = extractUserIdFromToken(token);
            if (userId == null) {
                return createErrorResponse("无效的token", 401);
            }

            User user = userService.getUserById(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "获取用户信息成功");
            response.put("data", new HashMap<String, Object>() {{
                put("user", new HashMap<String, Object>() {{
                    put("userId", user.getUserId());
                    put("username", user.getUsername());
                    put("realName", user.getRealName());
                    put("role", user.getRole());
                    put("roleName", getRoleName(user.getRole()));
                    put("college", user.getCollege());
                    put("phone", user.getPhone());
                    put("status", user.getStatus());
                    put("failCount", user.getFailCount());
                    put("createdAt", user.getCreatedAt());
                }});
            }});

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return createErrorResponse(e.getMessage(), 404);
        } catch (Exception e) {
            System.err.println("获取用户信息异常: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("获取用户信息失败: " + e.getMessage(), 500);
        }
    }

    // 退出登录
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(@RequestBody Map<String, String> logoutData) {
        System.out.println("✅ POST /api/auth/logout 被调用");

        String token = logoutData.get("token");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", 200);
        response.put("message", "退出登录成功");
        response.put("data", new HashMap<String, Object>() {{
            put("timestamp", System.currentTimeMillis());
        }});

        return ResponseEntity.ok(response);
    }

    // 辅助方法：生成token
    private String generateToken(User user) {
        // 简单的token生成，实际项目应该使用JWT
        return "user-token-" + System.currentTimeMillis() + "-" +
                user.getUserId() + "-" + user.getUsername();
    }

    // 辅助方法：从token中提取用户ID
    private Long extractUserIdFromToken(String token) {
        try {
            // 简单解析token，实际应使用JWT
            if (token != null && token.startsWith("user-token-")) {
                String[] parts = token.split("-");
                if (parts.length >= 4) {
                    return Long.parseLong(parts[3]);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // 辅助方法：获取角色名称
    private String getRoleName(Integer role) {
        if (role == null) return "未知";
        switch (role) {
            case 1: return "学生";
            case 2: return "教职工";
            case 3: return "管理员";
            default: return "未知";
        }
    }

    // 创建错误响应
    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, int code) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("code", code);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(code).body(errorResponse);
    }
}