package com.example.backend.controller;

import com.example.backend.dao.UserDao;
import com.example.backend.po.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")  // 完整路径
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserDao userDao;

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        System.out.println("✅ GET /api/auth/test 被调用");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("code", 200);
        response.put("message", "后端服务运行正常！");
        response.put("timestamp", System.currentTimeMillis());
        response.put("data", new HashMap<String, Object>() {{
            put("service", "Auth Service");
            put("status", "active");
            put("userCount", userDao.count());
        }});

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        System.out.println("✅ POST /api/auth/login 被调用");
        System.out.println("📝 请求数据: " + loginData);

        try {
            String username = loginData.get("username");
            String password = loginData.get("password");

            // 验证输入
            if (username == null || username.trim().isEmpty()) {
                return createErrorResponse("用户名不能为空", 400);
            }

            if (password == null || password.trim().isEmpty()) {
                return createErrorResponse("密码不能为空", 400);
            }

            // 查找用户
            Optional<User> userOptional = userDao.findByUsername(username);

            if (userOptional.isEmpty()) {
                System.out.println("❌ 用户不存在: " + username);
                return createErrorResponse("用户不存在", 404);
            }

            User user = userOptional.get();

            // 验证密码
            if (!user.getPassword().equals(password)) {
                System.out.println("❌ 密码错误: " + username);
                return createErrorResponse("密码错误", 401);
            }

            // 登录成功
            System.out.println("✅ 登录成功: " + username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "登录成功");
            response.put("data", new HashMap<String, Object>() {{
                put("token", "jwt-token-" + System.currentTimeMillis() + "-" + username);
                put("user", new HashMap<String, Object>() {{
                    put("id", user.getId());
                    put("username", user.getUsername());
                    put("email", user.getEmail());
                    put("role", user.getRole());
                }});
                put("expiresIn", 86400000); // 24小时
            }});

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("登录处理异常: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("服务器内部错误: " + e.getMessage(), 500);
        }
    }

    @PostMapping("/simple-login")
    public ResponseEntity<Map<String, Object>> simpleLogin(
            @RequestParam String username,
            @RequestParam String password) {
        System.out.println("✅ POST /api/auth/simple-login 被调用");
        System.out.println("📝 请求参数: username=" + username + ", password=[PROTECTED]");

        return login(new HashMap<String, String>() {{
            put("username", username);
            put("password", password);
        }});
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> registerData) {
        System.out.println("✅ POST /api/auth/register 被调用");

        try {
            String username = registerData.get("username");
            String password = registerData.get("password");
            String email = registerData.get("email");

            // 验证输入
            if (username == null || username.trim().isEmpty()) {
                return createErrorResponse("用户名不能为空", 400);
            }

            if (password == null || password.trim().isEmpty()) {
                return createErrorResponse("密码不能为空", 400);
            }

            if (username.length() < 3) {
                return createErrorResponse("用户名至少3个字符", 400);
            }

            if (password.length() < 6) {
                return createErrorResponse("密码至少6个字符", 400);
            }

            // 检查用户名是否已存在
            if (userDao.existsByUsername(username)) {
                return createErrorResponse("用户名已存在", 400);
            }

            // 创建新用户
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(email);
            user.setRole("USER");

            User savedUser = userDao.save(user);

            System.out.println("✅ 用户注册成功: " + username);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 201);
            response.put("message", "注册成功");
            response.put("data", new HashMap<String, Object>() {{
                put("id", savedUser.getId());
                put("username", savedUser.getUsername());
                put("email", savedUser.getEmail());
                put("role", savedUser.getRole());
            }});

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("注册处理异常: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("注册失败: " + e.getMessage(), 500);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        System.out.println("✅ GET /api/auth/users 被调用");

        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("code", 200);
            response.put("message", "获取用户列表成功");
            response.put("data", new HashMap<String, Object>() {{
                put("users", userDao.findAll());
                put("total", userDao.count());
            }});

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("获取用户列表异常: " + e.getMessage());
            e.printStackTrace();
            return createErrorResponse("获取用户列表失败: " + e.getMessage(), 500);
        }
    }

    private ResponseEntity<Map<String, Object>> createErrorResponse(String message, int code) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("code", code);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(code).body(errorResponse);
    }
}