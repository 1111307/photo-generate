-- 创建数据库
CREATE DATABASE IF NOT EXISTS photo_generate DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE photo_generate;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `role` tinyint NOT NULL DEFAULT '0' COMMENT '角色：0-普通用户，1-管理员',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 图片模板表
CREATE TABLE IF NOT EXISTS `photo_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `image_path` varchar(255) NOT NULL COMMENT '模板图片路径',
  `text_x` int NOT NULL DEFAULT '0' COMMENT '文字区域X坐标',
  `text_y` int NOT NULL DEFAULT '0' COMMENT '文字区域Y坐标',
  `text_width` int NOT NULL DEFAULT '0' COMMENT '文字区域宽度',
  `text_height` int NOT NULL DEFAULT '0' COMMENT '文字区域高度',
  `font_size` int NOT NULL DEFAULT '24' COMMENT '字体大小',
  `font_color` varchar(20) NOT NULL DEFAULT '#000000' COMMENT '字体颜色（十六进制）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片模板表';

-- 使用记录表
CREATE TABLE IF NOT EXISTS `usage_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `operation_type` tinyint NOT NULL COMMENT '操作类型：1-单次生成，2-批量生成',
  `count` int NOT NULL DEFAULT '1' COMMENT '生成数量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用记录表';

-- 插入默认管理员账号（密码：admin123，使用BCrypt加密）
INSERT INTO `user` (`username`, `password`, `email`, `role`) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@example.com', 1)
ON DUPLICATE KEY UPDATE `username`=`username`;