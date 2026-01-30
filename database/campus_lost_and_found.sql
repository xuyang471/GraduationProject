-- ============================================
-- 校园智能失物招领系统 数据库脚本
-- 文件名: campus_lost_and_found.sql
-- 创建时间: 2024年3月
-- 版本: 1.0
-- ============================================

-- 1. 创建数据库
DROP DATABASE IF EXISTS campus_lost_and_found;
CREATE DATABASE IF NOT EXISTS campus_lost_and_found 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE campus_lost_and_found;

-- ============================================
-- 2. 创建数据表
-- ============================================

-- 2.1 用户表
CREATE TABLE IF NOT EXISTS user (
    user_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名（学号/工号）',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    real_name VARCHAR(20) NOT NULL COMMENT '真实姓名',
    email VARCHAR(100) NOT NULL COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    role ENUM('student', 'teacher', 'admin') DEFAULT 'student' COMMENT '用户角色',
    college VARCHAR(100) COMMENT '学院/部门',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    status TINYINT(1) DEFAULT 1 COMMENT '账号状态（1正常，0冻结）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2.2 地点表
CREATE TABLE IF NOT EXISTS location (
    location_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '地点ID',
    campus VARCHAR(50) NOT NULL COMMENT '校区',
    building VARCHAR(100) NOT NULL COMMENT '楼宇/建筑',
    floor VARCHAR(20) COMMENT '楼层',
    room VARCHAR(50) COMMENT '房间/具体位置',
    description VARCHAR(255) COMMENT '地点描述',
    coordinates POINT COMMENT '地理坐标（可选）',
    is_active TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    INDEX idx_campus (campus),
    INDEX idx_building (building)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地点表';

-- 2.3 物品分类表
CREATE TABLE IF NOT EXISTS category (
    category_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    category_name VARCHAR(50) UNIQUE NOT NULL COMMENT '分类名称',
    parent_id INT DEFAULT NULL COMMENT '父分类ID',
    icon_url VARCHAR(255) COMMENT '分类图标',
    description VARCHAR(255) COMMENT '分类描述',
    INDEX idx_parent_id (parent_id),
    FOREIGN KEY (parent_id) REFERENCES category(category_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品分类表';

-- 2.4 失物/招领信息表
CREATE TABLE IF NOT EXISTS item (
    item_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '物品ID',
    user_id INT NOT NULL COMMENT '发布者ID',
    type ENUM('lost', 'found') NOT NULL COMMENT '类型：丢失/捡到',
    title VARCHAR(100) NOT NULL COMMENT '物品标题',
    description TEXT NOT NULL COMMENT '详细描述',
    category_id INT COMMENT '物品分类',
    location_id INT COMMENT '丢失/捡到地点',
    lost_found_time DATETIME NOT NULL COMMENT '丢失/捡到时间',
    image_urls JSON COMMENT '图片URL数组',
    status ENUM('pending', 'matched', 'claimed', 'closed') DEFAULT 'pending' COMMENT '物品状态',
    text_embedding BLOB COMMENT '文本语义向量',
    image_embedding BLOB COMMENT '图像特征向量',
    match_score FLOAT DEFAULT 0.0 COMMENT '匹配得分',
    is_public TINYINT(1) DEFAULT 1 COMMENT '是否公开显示',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(category_id) ON DELETE SET NULL,
    FOREIGN KEY (location_id) REFERENCES location(location_id) ON DELETE SET NULL,
    INDEX idx_type_status (type, status),
    INDEX idx_location_time (location_id, lost_found_time),
    INDEX idx_match_score (match_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='失物/招领信息表';

-- 2.5 匹配记录表
CREATE TABLE IF NOT EXISTS match_record (
    match_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '匹配记录ID',
    lost_item_id INT NOT NULL COMMENT '丢失物品ID',
    found_item_id INT NOT NULL COMMENT '招领物品ID',
    text_similarity FLOAT NOT NULL COMMENT '文本相似度',
    image_similarity FLOAT NOT NULL COMMENT '图像相似度',
    final_score FLOAT NOT NULL COMMENT '综合匹配分',
    is_recommended TINYINT(1) DEFAULT 0 COMMENT '是否已推荐',
    recommended_at TIMESTAMP NULL COMMENT '推荐时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '匹配计算时间',
    UNIQUE KEY uk_lost_found (lost_item_id, found_item_id),
    FOREIGN KEY (lost_item_id) REFERENCES item(item_id) ON DELETE CASCADE,
    FOREIGN KEY (found_item_id) REFERENCES item(item_id) ON DELETE CASCADE,
    INDEX idx_final_score (final_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='匹配记录表';

-- 2.6 认领记录表
CREATE TABLE IF NOT EXISTS claim_record (
    claim_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '认领ID',
    item_id INT NOT NULL COMMENT '物品ID',
    applicant_id INT NOT NULL COMMENT '申请人ID',
    claim_reason TEXT NOT NULL COMMENT '认领理由',
    proof_images JSON COMMENT '证明材料图片',
    status ENUM('pending', 'approved', 'rejected', 'canceled') DEFAULT 'pending' COMMENT '审核状态',
    admin_id INT COMMENT '审核管理员ID',
    review_notes TEXT COMMENT '审核备注',
    reviewed_at TIMESTAMP NULL COMMENT '审核时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (item_id) REFERENCES item(item_id) ON DELETE CASCADE,
    FOREIGN KEY (applicant_id) REFERENCES user(user_id) ON DELETE CASCADE,
    FOREIGN KEY (admin_id) REFERENCES user(user_id) ON DELETE SET NULL,
    INDEX idx_item_status (item_id, status),
    INDEX idx_applicant (applicant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='认领记录表';

-- 2.7 系统日志表
CREATE TABLE IF NOT EXISTS system_log (
    log_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id INT COMMENT '操作用户ID',
    log_type ENUM('login', 'logout', 'publish', 'claim', 'admin_op', 'error', 'system') NOT NULL COMMENT '日志类型',
    operation VARCHAR(100) NOT NULL COMMENT '操作描述',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '浏览器信息',
    request_params JSON COMMENT '请求参数',
    response_code INT COMMENT '响应状态码',
    error_message TEXT COMMENT '错误信息',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    FOREIGN KEY (user_id) REFERENCES user(user_id) ON DELETE SET NULL,
    INDEX idx_log_type_time (log_type, created_at),
    INDEX idx_user_time (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志表';

-- ============================================
-- 3. 插入示例数据
-- ============================================

-- 3.1 插入用户数据
INSERT INTO user (user_id, username, password, real_name, email, phone, role, college, avatar_url, status) VALUES
(1, 'admin001', '$2a$10$ABC123def456ghi789jkl0', '张明', 'admin@neu.edu.cn', '13800000001', 'admin', '软件学院', '/avatars/admin.jpg', 1),
(2, 'zhangli', '$2a$10$ABC123def456ghi789jkl0', '张丽', 'zhangli@neu.edu.cn', '13800000002', 'admin', '后勤管理处', '/avatars/default.jpg', 1),
(3, '20226669', '$2a$10$ABC123def456ghi789jkl0', '徐洋', '20226669@stu.neu.edu.cn', '13800000003', 'student', '软件学院', '/avatars/student1.jpg', 1),
(4, '20228888', '$2a$10$ABC123def456ghi789jkl0', '李华', '20228888@stu.neu.edu.cn', '13800000004', 'student', '计算机学院', '/avatars/student2.jpg', 1),
(5, '20229999', '$2a$10$ABC123def456ghi789jkl0', '王芳', '20229999@stu.neu.edu.cn', '13800000005', 'student', '信息学院', '/avatars/student3.jpg', 1),
(6, 't1001', '$2a$10$ABC123def456ghi789jkl0', '陈教授', 'chen@neu.edu.cn', '13800000006', 'teacher', '软件学院', '/avatars/teacher1.jpg', 1),
(7, '20227777', '$2a$10$ABC123def456ghi789jkl0', '刘强', '20227777@stu.neu.edu.cn', '13800000007', 'student', '机械学院', '/avatars/student4.jpg', 0);

-- 3.2 插入地点数据
INSERT INTO location (location_id, campus, building, floor, room, description, is_active) VALUES
(1, '南湖校区', '第一教学楼', '1', '101教室', '靠近正门的教室', 1),
(2, '南湖校区', '第一教学楼', '2', '201教室', '二楼东侧教室', 1),
(3, '南湖校区', '图书馆', '1', '阅览室A区', '靠窗座位区域', 1),
(4, '南湖校区', '图书馆', '3', '电子阅览室', '计算机区域', 1),
(5, '南湖校区', '学生一食堂', '1', '快餐窗口', '靠近入口的窗口', 1),
(6, '南湖校区', '学生一食堂', '2', '自助餐区', '二楼楼梯旁', 1),
(7, '南湖校区', '体育馆', '1', '篮球场', '主场地', 1),
(8, '南湖校区', '软件学院楼', '5', '502实验室', '软件工程实验室', 1),
(9, '南湖校区', '信息学院楼', '3', '会议室', '三楼东侧', 1),
(10, '南湖校区', '操场', '户外', '跑道起点', '100米起点处', 1),
(11, '浑南校区', '信息学馆', '2', 'B207教室', '多媒体教室', 1),
(12, '浑南校区', '生命科学楼', '1', '大厅', '入口休息区', 1),
(13, '浑南校区', '学生二食堂', '1', '面食窗口', '最里面的窗口', 1),
(14, '浑南校区', '图书馆', '2', '社科书库', '靠南书架', 1);

-- 3.3 插入分类数据
INSERT INTO category (category_id, category_name, parent_id, icon_url, description) VALUES
(1, '电子产品', NULL, '/icons/electronic.png', '手机、电脑、平板等'),
(2, '学习用品', NULL, '/icons/study.png', '书籍、文具等'),
(3, '证件卡片', NULL, '/icons/card.png', '身份证、学生卡、银行卡等'),
(4, '生活用品', NULL, '/icons/life.png', '水杯、雨伞、钥匙等'),
(5, '衣物配饰', NULL, '/icons/clothes.png', '衣服、帽子、围巾等'),
(6, '其他物品', NULL, '/icons/other.png', '其他未分类物品'),
(7, '手机', 1, '/icons/phone.png', '智能手机'),
(8, '笔记本电脑', 1, '/icons/laptop.png', '便携式电脑'),
(9, '平板电脑', 1, '/icons/tablet.png', 'iPad等'),
(10, '耳机', 1, '/icons/earphone.png', '有线/无线耳机'),
(11, '充电器', 1, '/icons/charger.png', '各类充电设备'),
(12, '教科书', 2, '/icons/book.png', '课程教材'),
(13, '笔记本', 2, '/icons/notebook.png', '纸质笔记本'),
(14, '文具', 2, '/icons/pen.png', '笔、尺、橡皮等'),
(15, '计算器', 2, '/icons/calculator.png', '科学计算器'),
(16, '校园卡', 3, '/icons/campus_card.png', '学生证/一卡通'),
(17, '身份证', 3, '/icons/id_card.png', '居民身份证'),
(18, '银行卡', 3, '/icons/bank_card.png', '储蓄卡/信用卡'),
(19, '水杯', 4, '/icons/cup.png', '保温杯、塑料杯'),
(20, '雨伞', 4, '/icons/umbrella.png', '晴雨伞'),
(21, '钥匙', 4, '/icons/key.png', '门钥匙、车钥匙'),
(22, '书包', 4, '/icons/bag.png', '双肩包、手提包');

-- 3.4 插入物品数据
INSERT INTO item (item_id, user_id, type, title, description, category_id, location_id, lost_found_time, image_urls, status, is_public, created_at) VALUES
(1, 3, 'lost', '黑色华为Mate 40手机', '黑色华为手机，手机壳是蓝色的，昨天下午丢失，内有重要资料', 7, 3, '2024-03-20 14:30:00', '["/items/phone1_1.jpg", "/items/phone1_2.jpg"]', 'pending', 1, '2024-03-20 15:00:00'),
(2, 4, 'lost', '《软件工程》教材', '红色封面，第二版，王老师课程用书，内有笔记', 12, 1, '2024-03-21 09:15:00', '["/items/book1.jpg"]', 'pending', 1, '2024-03-21 10:00:00'),
(3, 5, 'lost', '校园卡（卡号：20229999）', '南湖校区校园卡，姓名王芳，有卡通卡贴', 16, 5, '2024-03-22 12:00:00', '["/items/card1.jpg"]', 'matched', 1, '2024-03-22 12:30:00'),
(4, 6, 'lost', '黑色ThinkPad笔记本电脑', 'X1 Carbon型号，电源适配器一起丢失，贴有软件学院贴纸', 8, 8, '2024-03-19 16:45:00', '["/items/laptop1.jpg"]', 'pending', 1, '2024-03-19 17:20:00'),
(5, 3, 'lost', '蓝色保温杯', '蓝色膳魔师保温杯，杯身有轻微划痕，盖子银色', 19, 7, '2024-03-23 08:30:00', '["/items/cup1.jpg"]', 'claimed', 1, '2024-03-23 09:00:00'),
(6, 4, 'found', '捡到一部黑色手机', '在图书馆一楼捡到，黑色手机，无密码锁屏', 7, 3, '2024-03-20 15:30:00', '["/items/phone2.jpg"]', 'pending', 1, '2024-03-20 16:00:00'),
(7, 5, 'found', '捡到一本软件工程书', '红色封面，第一教学楼捡到，内有书签', 12, 1, '2024-03-21 10:30:00', '["/items/book2.jpg"]', 'pending', 1, '2024-03-21 11:00:00'),
(8, 6, 'found', '捡到一张校园卡', '卡号尾号9999，在食堂捡到', 16, 5, '2024-03-22 12:45:00', '["/items/card2.jpg"]', 'matched', 1, '2024-03-22 13:00:00'),
(9, 4, 'found', '捡到一副无线耳机', 'AirPods Pro，白色，右耳有划痕', 10, 4, '2024-03-24 13:15:00', '["/items/earphone1.jpg"]', 'pending', 1, '2024-03-24 13:30:00'),
(10, 5, 'found', '捡到一个蓝色水杯', '蓝色保温杯，在体育馆篮球场捡到', 19, 7, '2024-03-23 09:30:00', '["/items/cup2.jpg"]', 'claimed', 1, '2024-03-23 10:00:00');

-- 3.5 插入匹配记录数据
INSERT INTO match_record (match_id, lost_item_id, found_item_id, text_similarity, image_similarity, final_score, is_recommended, recommended_at, created_at) VALUES
(1, 3, 8, 0.92, 0.85, 0.89, 1, '2024-03-22 13:30:00', '2024-03-22 13:10:00'),
(2, 5, 10, 0.78, 0.91, 0.84, 1, '2024-03-23 10:15:00', '2024-03-23 10:05:00'),
(3, 1, 6, 0.65, 0.70, 0.67, 1, '2024-03-20 16:30:00', '2024-03-20 16:20:00'),
(4, 2, 7, 0.82, 0.75, 0.79, 0, NULL, '2024-03-21 11:30:00'),
(5, 1, 9, 0.30, 0.25, 0.28, 0, NULL, '2024-03-24 14:00:00'),
(6, 4, 6, 0.40, 0.35, 0.38, 0, NULL, '2024-03-20 17:00:00');

-- 3.6 插入认领记录数据
INSERT INTO claim_record (claim_id, item_id, applicant_id, claim_reason, proof_images, status, admin_id, review_notes, reviewed_at, created_at) VALUES
(1, 5, 5, '这是我的蓝色保温杯，杯底有我贴的姓名贴', '["/proofs/cup_proof1.jpg"]', 'approved', 1, '身份验证通过，物品描述一致', '2024-03-23 11:00:00', '2024-03-23 10:30:00'),
(2, 3, 5, '这是我的校园卡，卡号20229999', '["/proofs/card_proof1.jpg"]', 'pending', NULL, NULL, NULL, '2024-03-22 14:00:00'),
(3, 1, 6, '我丢失的手机与此描述相似', '["/proofs/phone_proof1.jpg"]', 'rejected', 2, '手机型号不符，IMEI号不匹配', '2024-03-20 17:30:00', '2024-03-20 17:00:00'),
(4, 2, 3, '可能是我同学的书', '["/proofs/book_proof1.jpg"]', 'canceled', NULL, '用户主动取消', NULL, '2024-03-21 12:00:00'),
(5, 6, 3, '这很像我丢失的华为手机', '["/proofs/phone_proof2.jpg"]', 'pending', NULL, NULL, NULL, '2024-03-20 18:00:00');

-- 3.7 插入系统日志数据
INSERT INTO system_log (log_id, user_id, log_type, operation, ip_address, user_agent, request_params, response_code, created_at) VALUES
(1, 3, 'login', '用户登录', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0)', '{"username": "20226669"}', 200, '2024-03-20 14:45:00'),
(2, 3, 'publish', '发布失物信息', '192.168.1.100', 'Mozilla/5.0 (Windows NT 10.0)', '{"item_id": 1, "type": "lost"}', 200, '2024-03-20 15:00:00'),
(3, 4, 'login', '用户登录', '192.168.1.101', 'Mozilla/5.0 (iPhone)', '{"username": "20228888"}', 200, '2024-03-20 15:30:00'),
(4, 4, 'publish', '发布招领信息', '192.168.1.101', 'Mozilla/5.0 (iPhone)', '{"item_id": 6, "type": "found"}', 200, '2024-03-20 16:00:00'),
(5, NULL, 'system', 'AI匹配任务执行', '127.0.0.1', 'Python-requests', '{"task": "daily_match"}', 200, '2024-03-20 16:20:00'),
(6, 1, 'login', '管理员登录', '10.0.0.1', 'Mozilla/5.0 (Windows NT 10.0)', '{"username": "admin001"}', 200, '2024-03-20 17:00:00'),
(7, 1, 'admin_op', '审核认领申请', '10.0.0.1', 'Mozilla/5.0 (Windows NT 10.0)', '{"claim_id": 3, "action": "reject"}', 200, '2024-03-20 17:30:00'),
(8, 5, 'claim', '提交认领申请', '192.168.1.102', 'Chrome/120.0.0.0', '{"item_id": 5, "claim_id": 1}', 200, '2024-03-23 10:30:00'),
(9, 5, 'login', '用户登录失败', '192.168.1.102', 'Chrome/120.0.0.0', '{"username": "20229999", "error": "wrong_password"}', 401, '2024-03-24 09:00:00'),
(10, 5, 'login', '用户登录成功', '192.168.1.102', 'Chrome/120.0.0.0', '{"username": "20229999"}', 200, '2024-03-24 09:01:00');

-- ============================================
-- 4. 重置自增序列（确保后续插入正常）
-- ============================================
ALTER TABLE user AUTO_INCREMENT = 100;
ALTER TABLE location AUTO_INCREMENT = 100;
ALTER TABLE category AUTO_INCREMENT = 100;
ALTER TABLE item AUTO_INCREMENT = 100;
ALTER TABLE match_record AUTO_INCREMENT = 100;
ALTER TABLE claim_record AUTO_INCREMENT = 100;
ALTER TABLE system_log AUTO_INCREMENT = 1000;

-- ============================================
-- 5. 创建视图（可选，便于查询）
-- ============================================

-- 5.1 物品详情视图
CREATE OR REPLACE VIEW v_item_detail AS
SELECT 
    i.item_id,
    i.type,
    i.title,
    i.description,
    i.status AS item_status,
    i.lost_found_time,
    i.image_urls,
    i.created_at AS publish_time,
    
    u.user_id,
    u.username,
    u.real_name,
    u.college,
    
    c.category_id,
    c.category_name,
    
    l.location_id,
    l.campus,
    l.building,
    l.floor,
    l.room
FROM item i
LEFT JOIN user u ON i.user_id = u.user_id
LEFT JOIN category c ON i.category_id = c.category_id
LEFT JOIN location l ON i.location_id = l.location_id;

-- 5.2 认领记录详情视图
CREATE OR REPLACE VIEW v_claim_detail AS
SELECT 
    cr.claim_id,
    cr.status AS claim_status,
    cr.claim_reason,
    cr.proof_images,
    cr.review_notes,
    cr.created_at AS claim_time,
    cr.reviewed_at,
    
    i.item_id,
    i.title AS item_title,
    i.type AS item_type,
    
    au.real_name AS applicant_name,
    au.college AS applicant_college,
    
    ru.real_name AS reviewer_name
FROM claim_record cr
LEFT JOIN item i ON cr.item_id = i.item_id
LEFT JOIN user au ON cr.applicant_id = au.user_id
LEFT JOIN user ru ON cr.admin_id = ru.user_id;

-- 5.3 匹配记录详情视图
CREATE OR REPLACE VIEW v_match_detail AS
SELECT 
    m.match_id,
    m.text_similarity,
    m.image_similarity,
    m.final_score,
    m.is_recommended,
    m.recommended_at,
    m.created_at AS match_time,
    
    li.item_id AS lost_item_id,
    li.title AS lost_item_title,
    li.description AS lost_item_desc,
    
    fi.item_id AS found_item_id,
    fi.title AS found_item_title,
    fi.description AS found_item_desc
FROM match_record m
LEFT JOIN item li ON m.lost_item_id = li.item_id
LEFT JOIN item fi ON m.found_item_id = fi.item_id;

-- ============================================
-- 6. 创建存储过程（示例）
-- ============================================

-- 6.1 统计某时间段内的失物数量
DELIMITER //
CREATE PROCEDURE sp_get_lost_statistics(
    IN p_start_date DATE,
    IN p_end_date DATE
)
BEGIN
    SELECT 
        DATE(lost_found_time) AS lost_date,
        l.campus,
        l.building,
        c.category_name,
        COUNT(*) AS lost_count
    FROM item i
    LEFT JOIN location l ON i.location_id = l.location_id
    LEFT JOIN category c ON i.category_id = c.category_id
    WHERE i.type = 'lost'
        AND DATE(i.lost_found_time) BETWEEN p_start_date AND p_end_date
    GROUP BY DATE(lost_found_time), l.campus, l.building, c.category_name
    ORDER BY lost_date DESC, lost_count DESC;
END //
DELIMITER ;

-- 6.2 获取用户的活跃度统计
DELIMITER //
CREATE PROCEDURE sp_get_user_activity(
    IN p_user_id INT
)
BEGIN
    SELECT 
        u.username,
        u.real_name,
        COUNT(DISTINCT CASE WHEN i.type = 'lost' THEN i.item_id END) AS lost_count,
        COUNT(DISTINCT CASE WHEN i.type = 'found' THEN i.item_id END) AS found_count,
        COUNT(DISTINCT cr.claim_id) AS claim_count,
        COUNT(DISTINCT CASE WHEN cr.status = 'approved' THEN cr.claim_id END) AS successful_claims
    FROM user u
    LEFT JOIN item i ON u.user_id = i.user_id
    LEFT JOIN claim_record cr ON u.user_id = cr.applicant_id
    WHERE u.user_id = p_user_id;
END //
DELIMITER ;

-- ============================================
-- 7. 数据库使用说明
-- ============================================

/*
数据库使用说明：
1. 执行本SQL文件创建数据库和表结构：
   mysql -u root -p < campus_lost_and_found.sql

2. 常用查询示例：
   -- 查询所有待匹配的失物
   SELECT * FROM v_item_detail WHERE type = 'lost' AND item_status = 'pending';
   
   -- 查询高匹配度的推荐
   SELECT * FROM v_match_detail WHERE is_recommended = 1 ORDER BY final_score DESC;
   
   -- 查询待审核的认领
   SELECT * FROM v_claim_detail WHERE claim_status = 'pending';

3. 调用存储过程：
   -- 统计最近7天的失物
   CALL sp_get_lost_statistics(CURDATE() - INTERVAL 7 DAY, CURDATE());
   
   -- 查看用户活跃度
   CALL sp_get_user_activity(3);

4. 数据验证：
   -- 检查数据一致性
   SELECT 'user' AS table_name, COUNT(*) AS count FROM user
   UNION ALL
   SELECT 'item', COUNT(*) FROM item
   UNION ALL
   SELECT 'match_record', COUNT(*) FROM match_record
   UNION ALL
   SELECT 'claim_record', COUNT(*) FROM claim_record;

注意事项：
1. 所有密码已加密存储（示例密码：123456）
2. 时间字段使用TIMESTAMP类型，自动记录创建和更新时间
3. JSON字段用于存储数组数据（如图片URL）
4. 外键约束确保数据完整性
*/

-- ============================================
-- 脚本结束
-- ============================================