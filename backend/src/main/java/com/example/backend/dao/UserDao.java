package com.example.backend.dao;

import com.example.backend.po.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDao extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    // 带筛选条件的分页查询
    @Query("SELECT u FROM User u WHERE " +
            "(:username IS NULL OR u.username LIKE %:username%) AND " +
            "(:realName IS NULL OR u.realName LIKE %:realName%) AND " +
            "(:role IS NULL OR u.role = :role) AND " +
            "(:status IS NULL OR u.status = :status) AND " +
            "(:college IS NULL OR u.college LIKE %:college%)")
    Page<User> findWithFilters(Pageable pageable,
                               @Param("username") String username,
                               @Param("realName") String realName,
                               @Param("role") Integer role,
                               @Param("status") Integer status,
                               @Param("college") String college);

    // 统计相关
    Long countByStatus(Integer status);

    Long countByRole(Integer role);

    Long countByFailCountGreaterThanEqual(Integer failCount);

    // 学院分布统计
    @Query("SELECT u.college, COUNT(u) FROM User u GROUP BY u.college")
    List<Object[]> countByCollege();

    // 搜索用户
    @Query("SELECT u FROM User u WHERE " +
            "u.username LIKE %:keyword% OR " +
            "u.realName LIKE %:keyword% OR " +
            "u.phone LIKE %:keyword% OR " +
            "u.college LIKE %:keyword%")
    List<User> searchUsers(@Param("keyword") String keyword);

    // 导出查询
    @Query("SELECT u FROM User u WHERE " +
            "(:username IS NULL OR u.username LIKE %:username%) AND " +
            "(:realName IS NULL OR u.realName LIKE %:realName%) AND " +
            "(:role IS NULL OR u.role = :role) AND " +
            "(:status IS NULL OR u.status = :status)")
    List<User> findForExport(@Param("username") String username,
                             @Param("realName") String realName,
                             @Param("role") Integer role,
                             @Param("status") Integer status);

    // 获取所有学院
    @Query("SELECT DISTINCT u.college FROM User u WHERE u.college IS NOT NULL AND u.college <> ''")
    List<String> findDistinctColleges();

}