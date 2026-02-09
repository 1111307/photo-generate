/*!40101 SET NAMES utf8 */;
/*!40014 SET FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET SQL_NOTES=0 */;

CREATE TABLE IF NOT EXISTS `photo_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属用户ID（UUID）',
  `template_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板名称',
  `image_path` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板图片路径',
  `text_x` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '文字区域X坐标（相对位置：0-1）',
  `text_y` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '文字区域Y坐标（相对位置：0-1）',
  `text_width` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '文字区域宽度（相对位置：0-1）',
  `text_height` decimal(10,4) NOT NULL DEFAULT '0.0000' COMMENT '文字区域高度（相对位置：0-1）',
  `cover_color` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '#ffffff' COMMENT '覆盖颜色（十六进制）',
  `font_size` int NOT NULL DEFAULT '37' COMMENT '字体大小',
  `font_color` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '#000000' COMMENT '字体颜色（十六进制）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图片模板表';

CREATE TABLE IF NOT EXISTS `usage_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID（UUID）',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `template_id` bigint DEFAULT NULL COMMENT '模板ID',
  `template_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '模板名称',
  `text_content` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文字内容',
  `operation_type` tinyint NOT NULL COMMENT '操作类型：1-单次生成，2-批量生成',
  `count` int NOT NULL DEFAULT '1' COMMENT '生成数量',
  `image_paths` text COLLATE utf8mb4_unicode_ci COMMENT '图片路径（JSON格式存储多个路径）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_template_id` (`template_id`),
  KEY `idx_template_name` (`template_name`)
) ENGINE=InnoDB AUTO_INCREMENT=130 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用记录表';

CREATE TABLE IF NOT EXISTS `user` (
  `id` varchar(36) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户ID（UUID）',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `role` tinyint NOT NULL DEFAULT '0' COMMENT '角色：0-普通用户，1-管理员',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标记：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 正确的INSERT语句（坐标已转换为相对位置）
INSERT INTO photo_template(id,user_id,template_name,image_path,text_x,text_y,text_width,text_height,cover_color,font_size,font_color,status,create_time,update_time,deleted) 
VALUES(1,'550e8400-e29b-41d4-a716-446655440000','灵犀模板','./templates/Lingxi_IMG_40571.png',0.0484,0.7634,1.0000,0.1398,'#ffffff',36,'#000000',1,'2026-01-27 10:51:47','2026-02-06 17:27:59',0);

INSERT INTO `user`(id,username,password,email,phone,role,create_time,update_time,deleted) 
VALUES('550e8400-e29b-41d4-a716-446655440000','admin','$2a$10$t978glPyg73O/6kDFbTXaODLMI7BHgT81DvcgU6p57NgJJLyJNQt6','admin@example.com',NULL,1,'2026-02-06 17:23:15','2026-02-06 17:23:15',0),
('fa78242efdfe2846e450a635582338fa','root','$2a$10$JwnBL.C9J5d.m5C.4VSE..YtmDnBDjtlaA9dTqtwsn8horCwN4JgW','','',0,'2026-02-08 14:32:02','2026-02-08 14:32:02',0);
