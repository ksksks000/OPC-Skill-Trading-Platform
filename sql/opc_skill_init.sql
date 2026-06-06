-- ============================================
-- OPC 技能交易平台 - 数据库建表脚本
-- 数据库名：opc_skill
-- ============================================

CREATE DATABASE IF NOT EXISTS opc_skill DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE opc_skill;

-- ============================================
-- 1. 员工/管理员表
-- ============================================
CREATE TABLE IF NOT EXISTS `employee` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username` VARCHAR(32) NOT NULL COMMENT '用户名',
    `name` VARCHAR(32) NOT NULL COMMENT '姓名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `phone` VARCHAR(11) DEFAULT NULL COMMENT '手机号',
    `sex` VARCHAR(2) DEFAULT NULL COMMENT '性别',
    `id_number` VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
    `status` INT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `create_user` BIGINT DEFAULT NULL COMMENT '创建人',
    `update_user` BIGINT DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工/管理员表';

-- 初始管理员（密码：123456，BCrypt加密）
INSERT INTO `employee` (`id`, `username`, `name`, `password`, `status`, `create_time`, `update_time`)
VALUES (1, 'admin', '管理员', '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jeeGm/TEZyj1Cq', 1, NOW(), NOW());

-- ============================================
-- 2. 用户表（买家/卖家共用）
-- ============================================
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username` VARCHAR(32) DEFAULT NULL COMMENT '用户名',
    `password` VARCHAR(255) DEFAULT NULL COMMENT '密码',
    `name` VARCHAR(32) DEFAULT NULL COMMENT '姓名',
    `phone` VARCHAR(11) DEFAULT NULL COMMENT '手机号',
    `sex` VARCHAR(2) DEFAULT NULL COMMENT '性别',
    `id_number` VARCHAR(18) DEFAULT NULL COMMENT '身份证号',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `role` INT DEFAULT 0 COMMENT '角色：0买家 1卖家 2管理员',
    `status` INT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    `create_time` DATETIME DEFAULT NULL COMMENT '注册时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 测试用户（密码：123456）
INSERT INTO `user` (`id`, `username`, `password`, `name`, `phone`, `role`, `status`, `create_time`)
VALUES
(1, 'buyer1', '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jeeGm/TEZyj1Cq', '张三', '13800001111', 0, 1, NOW()),
(2, 'seller1', '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jeeGm/TEZyj1Cq', '李四', '13800002222', 1, 1, NOW()),
(3, 'seller2', '$2a$10$N.ZOn9G6/YLFixAOPMg/h.z7pCu6v2XyFDtC4q.jeeGm/TEZyj1Cq', '王五', '13800003333', 1, 1, NOW());

-- ============================================
-- 3. 技能/服务商品表
-- ============================================
CREATE TABLE IF NOT EXISTS `skill_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` VARCHAR(128) NOT NULL COMMENT '技能名称',
    `description` TEXT DEFAULT NULL COMMENT '技能描述（富文本）',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
    `status` INT DEFAULT 1 COMMENT '状态：1上架 0下架',
    `tags` VARCHAR(512) DEFAULT NULL COMMENT '标签（JSON格式）',
    `image` VARCHAR(255) DEFAULT NULL COMMENT '封面图',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `create_user` BIGINT DEFAULT NULL COMMENT '创建人',
    `update_user` BIGINT DEFAULT NULL COMMENT '更新人',
    PRIMARY KEY (`id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能/服务商品表';

-- 测试技能数据
INSERT INTO `skill_info` (`id`, `name`, `description`, `price`, `category_id`, `seller_id`, `status`, `tags`, `image`, `create_time`, `update_time`)
VALUES
(1, '心理咨询-深度对话', '国家二级心理咨询师，擅长焦虑、抑郁、人际关系等问题的深度疏导。采用认知行为疗法(CBT)与正念结合的方式，帮助您找到内心的平静。每次咨询60分钟，提供后续文字跟进支持。', 299.00, 1, 2, 1, '["心理咨询","二级咨询师","焦虑疏导","认知行为疗法"]', NULL, NOW(), NOW()),
(2, '简历优化-斩获大厂Offer', '10年HR经验，曾任职于BAT大厂。深度分析您的简历问题，从结构、内容、关键词三个维度全面优化，确保通过ATS系统筛选。包含2次修改+1次模拟面试指导。', 199.00, 2, 2, 1, '["简历优化","大厂面试","HR经验","职业规划"]', NULL, NOW(), NOW()),
(3, '塔罗牌占卜-指引人生方向', '专业塔罗师，8年占卜经验。通过韦特塔罗牌阵，为您解读爱情、事业、财运的发展趋势。提供详细的文字解读报告，含建议与行动指南。', 88.00, 3, 3, 1, '["塔罗牌","占卜","韦特塔罗","运势解读"]', NULL, NOW(), NOW()),
(4, 'Python编程入门陪跑', '5年Python开发经验，从零基础到独立写项目。包含：基础语法、数据结构、爬虫实战、数据分析入门。每周2次1对1视频辅导+日常答疑，为期4周。', 599.00, 2, 3, 1, '["Python","编程教学","零基础","一对一辅导"]', NULL, NOW(), NOW()),
(5, '商业计划书撰写', '曾帮助20+创业团队获得天使轮融资。从市场分析、商业模式、财务预测到路演PPT，全流程协助。包含3次深度沟通+2次修改迭代。', 1299.00, 4, 2, 1, '["商业计划书","融资","创业指导","天使轮"]', NULL, NOW(), NOW()),
(6, '情感挽回-亲密关系修复', '专业情感咨询师，擅长亲密关系修复与沟通技巧培训。通过深度对话找到关系裂痕的根源，提供可执行的修复方案。全程保密，尊重隐私。', 399.00, 1, 3, 1, '["情感咨询","亲密关系","沟通技巧","关系修复"]', NULL, NOW(), NOW()),
(7, 'UI设计-App界面全套', '5年UI/UX设计经验，服务过多个千万级用户产品。提供：需求分析→线框图→高保真设计→切图标注。支持iOS+Android双平台，含2次修改。', 899.00, 5, 2, 1, '["UI设计","App设计","用户体验","高保真"]', NULL, NOW(), NOW()),
(8, '法律咨询-劳动纠纷', '执业律师，专注劳动法领域10年。提供劳动合同审查、劳动仲裁代理、工伤赔偿咨询等服务。首次咨询30分钟免费，后续按次收费。', 199.00, 6, 3, 1, '["法律咨询","劳动纠纷","劳动合同","仲裁代理"]', NULL, NOW(), NOW());

-- ============================================
-- 4. 订单表
-- ============================================
CREATE TABLE IF NOT EXISTS `orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `number` VARCHAR(64) NOT NULL COMMENT '订单号',
    `status` INT DEFAULT 1 COMMENT '订单状态：1待付款 2待接单 3已接单/服务中 5已完成 6已取消',
    `user_id` BIGINT NOT NULL COMMENT '下单用户ID',
    `seller_id` BIGINT DEFAULT NULL COMMENT '卖家ID',
    `chat_record_id` BIGINT DEFAULT NULL COMMENT '关联聊天记录ID',
    `delivery_status` INT DEFAULT 0 COMMENT '服务交付状态：0待服务 1服务中 2待确认 3已完成',
    `order_time` DATETIME DEFAULT NULL COMMENT '下单时间',
    `checkout_time` DATETIME DEFAULT NULL COMMENT '结账时间',
    `pay_method` INT DEFAULT NULL COMMENT '支付方式：1微信 2支付宝',
    `pay_status` INT DEFAULT 0 COMMENT '支付状态：0未支付 1已支付 2退款',
    `amount` DECIMAL(10,2) DEFAULT NULL COMMENT '实收金额',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `cancel_reason` VARCHAR(255) DEFAULT NULL COMMENT '取消原因',
    `rejection_reason` VARCHAR(255) DEFAULT NULL COMMENT '拒绝原因',
    `cancel_time` DATETIME DEFAULT NULL COMMENT '取消时间',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_seller_id` (`seller_id`),
    KEY `idx_number` (`number`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ============================================
-- 5. 订单明细表
-- ============================================
CREATE TABLE IF NOT EXISTS `order_detail` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name` VARCHAR(128) DEFAULT NULL COMMENT '技能名称',
    `image` VARCHAR(255) DEFAULT NULL COMMENT '图片',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `skill_id` BIGINT DEFAULT NULL COMMENT '技能ID',
    `number` INT DEFAULT 1 COMMENT '数量',
    `amount` DECIMAL(10,2) DEFAULT NULL COMMENT '金额',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_skill_id` (`skill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ============================================
-- 6. 聊天消息表
-- ============================================
CREATE TABLE IF NOT EXISTS `chat_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `sender_id` BIGINT NOT NULL COMMENT '发送者用户ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者用户ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `type` VARCHAR(16) DEFAULT 'text' COMMENT '消息类型：text文本 image图片',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0未读 1已读',
    PRIMARY KEY (`id`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_receiver_id` (`receiver_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';
