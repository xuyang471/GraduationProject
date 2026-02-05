package com.example.backend.service;

import com.example.backend.po.User;
import com.example.backend.dao.UserDao;
import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.LoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserDao userDao;

    // 存储token和用户的映射（生产环境应使用Redis）
    private Map<String, User> tokenStore = new HashMap<>();

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // 查找用户
        User user = userDao.findByUsername(username)
                .orElse(null);

        if (user == null || !user.getPassword().equals(password)) {
            return new LoginResponse(false, "用户名或密码错误");
        }

        // 生成token
        String token = generateToken(user);

        return new LoginResponse(
                true,
                "登录成功",
                token,
                user.getUsername(),
                user.getRole()
        );
    }

    @Override
    public String generateToken(User user) {
        String token = "token-" + UUID.randomUUID().toString();
        tokenStore.put(token, user);
        return token;
    }

    @Override
    public void logout(String token) {
        tokenStore.remove(token);
    }

    @Override
    public User getCurrentUser(String token) {
        return tokenStore.get(token);
    }

    @Override
    public boolean validateToken(String token) {
        return tokenStore.containsKey(token);
    }
}