package com.example.backend.po;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", unique = true, nullable = false, length = 20)
    private String username;

    @Column(name = "password", nullable = false, length = 32)
    private String password;

    @Column(name = "real_name", nullable = false, length = 20)
    private String realName;

    @Column(name = "phone", length = 11)
    private String phone;

    @Column(name = "role", nullable = false)
    private Integer role = 1; // 1:学生 2:教职工 3:管理员

    @Column(name = "college", length = 50)
    private String college;

    @Column(name = "status", nullable = false)
    private Integer status = 1; // 1正常，0冻结

    @Column(name = "fail_count")
    private Integer failCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 构造方法
    public User() {
        this.createdAt = LocalDateTime.now();
    }

    public User(String username, String password, String realName) {
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.createdAt = LocalDateTime.now();
    }

    // Getter和Setter
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getFailCount() {
        return failCount;
    }

    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // 辅助方法
    public boolean isActive() {
        return this.status == 1;
    }

    public boolean isAdmin() {
        return this.role == 3;
    }

    public boolean isTeacher() {
        return this.role == 2;
    }

    public boolean isStudent() {
        return this.role == 1;
    }

    public void incrementFailCount() {
        this.failCount = (this.failCount == null) ? 1 : this.failCount + 1;
        if (this.failCount >= 5) {
            this.status = 0; // 自动冻结
        }
    }

    public void resetFailCount() {
        this.failCount = 0;
    }

    @Transient
    private Boolean firstLogin = true;

    // 计算属性：firstLogin 逻辑（如果需要）
    public Boolean getFirstLogin() {
        // 这里可以根据业务逻辑计算
        // 例如：密码是默认密码（账号后6位）
        if (password != null && username != null) {
            String defaultPassword = username.length() >= 6 ?
                    username.substring(username.length() - 6) : username;
            return password.equals(defaultPassword);
        }
        return true; // 默认值
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", realName='" + realName + '\'' +
                ", role=" + role +
                ", college='" + college + '\'' +
                ", status=" + status +
                '}';
    }
}