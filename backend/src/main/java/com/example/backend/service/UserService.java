package com.example.backend.service;

import com.example.backend.po.User;
import com.example.backend.dao.UserDao;
import com.example.backend.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    // 1. 获取所有用户
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    // 2. 分页查询用户
    public Page<User> getUsers(Pageable pageable,
                               String username,
                               String realName,
                               Integer role,
                               Integer status,
                               String college) {
        return userDao.findWithFilters(pageable, username, realName, role, status, college);
    }

    // 3. 根据ID获取用户
    public User getUserById(Long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + id));
    }

    // 4. 根据用户名获取用户
    public User getUserByUsername(String username) {
        return userDao.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在，用户名: " + username));
    }

    // 5. 创建用户（管理员手动添加）
    @Transactional
    public User createUser(UserDTO userDTO) {
        // 检查用户名是否已存在
        if (userDao.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("用户名（学号/工号）已存在: " + userDTO.getUsername());
        }

        // 创建用户对象
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword()); // 密码为账号后6位
        user.setRealName(userDTO.getRealName());
        user.setPhone(userDTO.getPhone());
        user.setRole(userDTO.getRole() != null ? userDTO.getRole() : 1); // 默认为学生
        user.setCollege(userDTO.getCollege());
        user.setStatus(userDTO.getActive() != null && userDTO.getActive() ? 1 : 0);
        user.setFailCount(0);
        user.setCreatedAt(LocalDateTime.now());

        return userDao.save(user);
    }

    // 6. 批量创建用户
    @Transactional
    public Map<String, Object> batchCreateUsers(List<UserDTO> userDTOs) {
        Map<String, Object> result = new HashMap<>();
        List<String> successList = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        for (UserDTO userDTO : userDTOs) {
            try {
                // 检查必填字段
                if (userDTO.getUsername() == null || userDTO.getUsername().trim().isEmpty()) {
                    errorList.add(userDTO.getRealName() + ": 用户名不能为空");
                    errorCount++;
                    continue;
                }

                // 检查用户名是否已存在
                if (userDao.existsByUsername(userDTO.getUsername())) {
                    errorList.add(userDTO.getUsername() + ": 用户名已存在");
                    errorCount++;
                    continue;
                }

                // 创建用户
                User user = new User();
                user.setUsername(userDTO.getUsername());
                // 设置默认密码（账号后6位）
                String username = userDTO.getUsername();
                String defaultPassword = username.length() >= 6 ?
                        username.substring(username.length() - 6) : username;
                user.setPassword(defaultPassword);
                user.setRealName(userDTO.getRealName() != null ? userDTO.getRealName() : "");
                user.setPhone(userDTO.getPhone());
                user.setRole(userDTO.getRole() != null ? userDTO.getRole() : 1);
                user.setCollege(userDTO.getCollege());
                user.setStatus(1); // 默认激活
                user.setFailCount(0);
                user.setCreatedAt(LocalDateTime.now());

                userDao.save(user);
                successList.add(userDTO.getUsername());
                successCount++;
            } catch (Exception e) {
                errorList.add(userDTO.getUsername() + ": " + e.getMessage());
                errorCount++;
            }
        }

        result.put("successCount", successCount);
        result.put("errorCount", errorCount);
        result.put("successUsers", successList);
        result.put("errorUsers", errorList);
        result.put("totalProcessed", userDTOs.size());

        return result;
    }

    // 7. 更新用户信息
    @Transactional
    public User updateUser(Long id, UserDTO userDTO) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + id));

        // 更新基本信息
        if (userDTO.getRealName() != null) {
            user.setRealName(userDTO.getRealName());
        }
        if (userDTO.getPhone() != null) {
            user.setPhone(userDTO.getPhone());
        }
        if (userDTO.getCollege() != null) {
            user.setCollege(userDTO.getCollege());
        }
        if (userDTO.getRole() != null) {
            user.setRole(userDTO.getRole());
        }
        if (userDTO.getActive() != null) {
            user.setStatus(userDTO.getActive() ? 1 : 0);
        }

        // 如果密码不为空且不是默认密码，则更新密码
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            // 检查是否是默认密码格式（账号后6位）
            String defaultPassword = user.getUsername().length() >= 6 ?
                    user.getUsername().substring(user.getUsername().length() - 6) : user.getUsername();

            if (!userDTO.getPassword().equals(defaultPassword)) {
                user.setPassword(userDTO.getPassword());
            }
        }

        return userDao.save(user);
    }

    // 8. 删除用户
    @Transactional
    public void deleteUser(Long id) {
        if (!userDao.existsById(id)) {
            throw new RuntimeException("用户不存在，ID: " + id);
        }

        // 检查用户是否有相关数据（可选）
        // 可以添加逻辑检查用户是否有失物、认领记录等

        userDao.deleteById(id);
    }

    // 9. 更新用户状态（冻结/解冻）
    @Transactional
    public User updateUserStatus(Long id, boolean active) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + id));

        user.setStatus(active ? 1 : 0);

        // 如果解冻用户，重置失败次数
        if (active) {
            user.setFailCount(0);
        }

        return userDao.save(user);
    }

    // 10. 重置密码
    @Transactional
    public User resetPassword(Long id) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在，ID: " + id));

        // 重置为账号后6位
        String username = user.getUsername();
        String defaultPassword = username.length() >= 6 ?
                username.substring(username.length() - 6) : username;
        user.setPassword(defaultPassword);

        // 标记需要首次登录修改密码
        // 注意：这里需要在User实体中添加firstLogin字段

        return userDao.save(user);
    }

    // 11. 搜索用户
    public List<User> searchUsers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return userDao.searchUsers(keyword.trim());
    }

    // 12. 获取用户统计信息
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 总用户数
        Long totalUsers = userDao.count();
        stats.put("totalUsers", totalUsers);

        // 活跃用户数
        Long activeUsers = userDao.countByStatus(1);
        stats.put("activeUsers", activeUsers);

        // 冻结用户数
        Long frozenUsers = userDao.countByStatus(0);
        stats.put("frozenUsers", frozenUsers);

        // 角色分布
        Long studentCount = userDao.countByRole(1);
        Long teacherCount = userDao.countByRole(2);
        Long adminCount = userDao.countByRole(3);
        stats.put("studentCount", studentCount);
        stats.put("teacherCount", teacherCount);
        stats.put("adminCount", adminCount);

        // 学院分布
        List<Object[]> collegeStats = userDao.countByCollege();
        Map<String, Long> collegeDistribution = new HashMap<>();
        for (Object[] row : collegeStats) {
            String college = (String) row[0];
            Long count = (Long) row[1];
            collegeDistribution.put(college != null ? college : "未设置", count);
        }
        stats.put("collegeDistribution", collegeDistribution);

        // 失败次数统计
        Long highFailUsers = userDao.countByFailCountGreaterThanEqual(3);
        stats.put("highFailUsers", highFailUsers);

        return stats;
    }

    // 13. 导出用户
    public List<User> exportUsers(String username, String realName, Integer role, Integer status) {
        return userDao.findForExport(username, realName, role, status);
    }

    // 14. 验证用户登录
    @Transactional
    public User authenticate(String username, String password) {
        User user = getUserByUsername(username);

        // 检查账号状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被冻结，请联系管理员");
        }

        // 检查密码
        if (!user.getPassword().equals(password)) {
            // 增加失败次数
            user.setFailCount(user.getFailCount() + 1);

            // 如果失败3次，冻结账号
            if (user.getFailCount() >= 3) {
                user.setStatus(0);
            }

            userDao.save(user);
            throw new RuntimeException("密码错误，剩余尝试次数: " + (3 - user.getFailCount()));
        }

        // 登录成功，重置失败次数
        if (user.getFailCount() > 0) {
            user.setFailCount(0);
            userDao.save(user);
        }

        return user;
    }

    // 15. 修改密码
    @Transactional
    public User changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);

        // 验证旧密码
        if (!user.getPassword().equals(oldPassword)) {
            throw new RuntimeException("原密码错误");
        }

        // 更新密码
        user.setPassword(newPassword);

        return userDao.save(user);
    }

    // 16. 检查用户名是否存在
    public boolean checkUsernameExists(String username) {
        return userDao.existsByUsername(username);
    }

    // 17. 初始化测试用户
    @Transactional
    public void initTestUsers() {
        if (userDao.count() == 0) {
            // 创建管理员
            User admin = new User("admin001", "in001", "张明");
            admin.setPhone("13800000001");
            admin.setRole(3); // 管理员
            admin.setCollege("软件学院");
            admin.setStatus(1);
            userDao.save(admin);

            // 创建学生
            User student1 = new User("20230001", "000001", "王涛");
            student1.setPhone("13800010001");
            student1.setRole(1); // 学生
            student1.setCollege("计算机学院");
            userDao.save(student1);

            User student2 = new User("20230002", "000002", "刘芳");
            student2.setPhone("13800010002");
            student2.setRole(1); // 学生
            student2.setCollege("软件学院");
            userDao.save(student2);

            // 创建教职工
            User teacher = new User("T1001", "T1001", "赵教授");
            teacher.setPhone("13800020001");
            teacher.setRole(2); // 教职工
            teacher.setCollege("软件学院");
            userDao.save(teacher);

            System.out.println("测试用户已初始化完成");
        }
    }

    // 18. 增加用户失败次数（用于认领失败）
    @Transactional
    public void incrementFailCount(Long userId) {
        User user = getUserById(userId);
        user.setFailCount(user.getFailCount() + 1);

        // 如果失败3次，冻结账号
        if (user.getFailCount() >= 3) {
            user.setStatus(0);
        }

        userDao.save(user);
    }

    // 19. 获取学院列表
    public List<String> getColleges() {
        return userDao.findDistinctColleges();
    }
}