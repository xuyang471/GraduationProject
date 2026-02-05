package com.example.backend.service;

import com.example.backend.po.User;
import com.example.backend.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public User getUserById(Long id) {
        return userDao.findById(id).orElse(null);
    }

    public User getUserByUsername(String username) {
        return userDao.findByUsername(username).orElse(null);
    }

    public User createUser(User user) {
        // 检查用户名是否已存在
        if (userDao.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        return userDao.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        user.setEmail(userDetails.getEmail());
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(userDetails.getPassword());
        }

        return userDao.save(user);
    }

    public void deleteUser(Long id) {
        userDao.deleteById(id);
    }

    public void initTestUsers() {
        // 初始化测试用户
        if (userDao.count() == 0) {
            User admin = new User("admin", "admin123");
            admin.setEmail("admin@example.com");
            admin.setRole("ADMIN");
            userDao.save(admin);

            User user = new User("user", "user123");
            user.setEmail("user@example.com");
            userDao.save(user);

            System.out.println("测试用户已初始化");
        }
    }
}