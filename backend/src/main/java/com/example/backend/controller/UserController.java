package com.example.backend.controller;

import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.UserDTO;
import com.example.backend.po.User;
import com.example.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:8080")
public class UserController {

    @Autowired
    private UserService userService;

    // 1. 创建用户（管理员手动添加）
    @PostMapping("/users")
    public ApiResponse<User> createUser(@RequestBody UserDTO userDTO) {
        try {
            // 验证必填字段
            if (userDTO.getUsername() == null || userDTO.getUsername().trim().isEmpty()) {
                return ApiResponse.error("用户名（学号/工号）不能为空");
            }
            if (userDTO.getRealName() == null || userDTO.getRealName().trim().isEmpty()) {
                return ApiResponse.error("真实姓名不能为空");
            }

            // 自动生成密码：默认使用账号后6位
            String username = userDTO.getUsername();
            String defaultPassword = username.length() >= 6 ?
                    username.substring(username.length() - 6) : username;
            userDTO.setPassword(defaultPassword);
            userDTO.setFirstLogin(true); // 标记首次登录需要修改密码

            User createdUser = userService.createUser(userDTO);
            return ApiResponse.success("用户创建成功，初始密码为账号后6位", createdUser);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // 2. 批量导入用户（从Excel等）
    @PostMapping("/users/batch")
    public ApiResponse<Map<String, Object>> batchCreateUsers(@RequestBody List<UserDTO> userDTOs) {
        try {
            Map<String, Object> result = userService.batchCreateUsers(userDTOs);
            return ApiResponse.success("批量导入完成", result);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // 3. 更新用户信息
    @PutMapping("/users/{id}")
    public ApiResponse<User> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        try {
            User updatedUser = userService.updateUser(id, userDTO);
            return ApiResponse.success("用户更新成功", updatedUser);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // 4. 删除用户
    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ApiResponse.success("用户删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error("删除用户失败: " + e.getMessage());
        }
    }

    // 5. 冻结/解冻用户账号
    @PatchMapping("/users/{id}/status")
    public ApiResponse<User> toggleUserStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        try {
            Boolean active = request.get("active");
            if (active == null) {
                return ApiResponse.error("状态参数不能为空");
            }
            User updatedUser = userService.updateUserStatus(id, active);
            String message = active ? "账号已解冻" : "账号已冻结";
            return ApiResponse.success(message, updatedUser);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // 6. 重置密码
    @PostMapping("/users/{id}/reset-password")
    public ApiResponse<User> resetPassword(@PathVariable Long id) {
        try {
            User user = userService.resetPassword(id);
            return ApiResponse.success("密码已重置为账号后6位", user);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // 7. 获取用户列表（支持分页和搜索）
    @GetMapping("/users")
    public ApiResponse<Page<User>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String college) {

        try {
            // 构建分页和排序
            Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ?
                    Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

            // 调用服务层查询
            Page<User> users = userService.getUsers(pageable, username, realName, role, status, college);
            return ApiResponse.success("获取用户列表成功", users);
        } catch (Exception e) {
            return ApiResponse.error("获取用户列表失败: " + e.getMessage());
        }
    }

    // 8. 获取用户详情
    @GetMapping("/users/{id}")
    public ApiResponse<User> getUser(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return ApiResponse.success("获取用户信息成功", user);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    // 9. 搜索用户（快速搜索）
    @GetMapping("/users/search")
    public ApiResponse<List<User>> searchUsers(@RequestParam String keyword) {
        try {
            List<User> users = userService.searchUsers(keyword);
            return ApiResponse.success("搜索完成", users);
        } catch (Exception e) {
            return ApiResponse.error("搜索失败: " + e.getMessage());
        }
    }

    // 10. 获取用户统计信息
    @GetMapping("/users/statistics")
    public ApiResponse<Map<String, Object>> getUserStatistics() {
        try {
            Map<String, Object> stats = userService.getUserStatistics();
            return ApiResponse.success("获取统计信息成功", stats);
        } catch (Exception e) {
            return ApiResponse.error("获取统计信息失败: " + e.getMessage());
        }
    }

    // 11. 导出用户列表
    @GetMapping("/users/export")
    public ApiResponse<List<User>> exportUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status) {

        try {
            List<User> users = userService.exportUsers(username, realName, role, status);
            return ApiResponse.success("导出数据准备完成", users);
        } catch (Exception e) {
            return ApiResponse.error("导出失败: " + e.getMessage());
        }
    }
}