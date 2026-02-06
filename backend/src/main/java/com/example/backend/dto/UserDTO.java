package com.example.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDTO {
    private String username;      // 学号/工号
    private String password;      // 密码
    private String realName;      // 真实姓名
    private String phone;         // 手机号
    private Integer role = 1;     // 1:学生 2:教职工 3:管理员
    private String college;       // 学院/部门
    private Boolean firstLogin = true; // 是否首次登录
    private Boolean active = true;     // 账号是否活跃
}

// 用户统计DTO
@Data
@NoArgsConstructor
class UserStatisticsDTO {
    private Long totalUsers;
    private Long activeUsers;
    private Long frozenUsers;
    private Long studentCount;
    private Long teacherCount;
    private Long adminCount;
    private Map<String, Long> collegeDistribution;
}