-- ========================================
-- 基于SpringBoot的校园智能问答系统 - 完整数据库初始化脚本
-- 包含：数据库创建、表结构、真实初始数据
-- ========================================

-- 删除已存在的数据库（重要！）
DROP DATABASE IF EXISTS `campus_qa_system`;

-- 创建数据库
CREATE DATABASE `campus_qa_system` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `campus_qa_system`;

-- ========================================
-- 1. 创建表结构
-- ========================================

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `student_id` varchar(30) DEFAULT NULL COMMENT '学号/工号',
  `role` varchar(20) NOT NULL DEFAULT 'STUDENT' COMMENT '角色(STUDENT-学生,ADMIN-管理员)',
  `gender` tinyint DEFAULT NULL COMMENT '性别(1-男,2-女)',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  `department` varchar(100) DEFAULT NULL COMMENT '学院/部门',
  `major` varchar(100) DEFAULT NULL COMMENT '专业/职位',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除,1-已删除)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_role` (`role`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `sys_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint DEFAULT NULL COMMENT '操作用户ID',
  `username` varchar(50) DEFAULT NULL COMMENT '操作用户名',
  `module` varchar(50) DEFAULT NULL COMMENT '操作模块',
  `operation_type` varchar(20) DEFAULT NULL COMMENT '操作类型(CREATE-新增,UPDATE-修改,DELETE-删除,SELECT-查询)',
  `description` varchar(255) DEFAULT NULL COMMENT '操作描述',
  `method` varchar(255) DEFAULT NULL COMMENT '请求方法',
  `request_params` text COMMENT '请求参数',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `duration` bigint DEFAULT NULL COMMENT '执行时长(毫秒)',
  `status` tinyint DEFAULT 1 COMMENT '操作状态(0-失败,1-成功)',
  `error_msg` text COMMENT '错误信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_module` (`module`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 消息通知表
CREATE TABLE IF NOT EXISTS `sys_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `title` varchar(100) NOT NULL COMMENT '通知标题',
  `content` text NOT NULL COMMENT '通知内容',
  `type` varchar(20) NOT NULL COMMENT '通知类型(SYSTEM-系统通知,ACTIVITY-活动通知,NEWS-资讯通知,PERSONAL-个人消息)',
  `receiver_id` bigint DEFAULT NULL COMMENT '接收用户ID(为空则为全体用户)',
  `sender_id` bigint DEFAULT NULL COMMENT '发送用户ID',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读(0-未读,1-已读)',
  `priority` tinyint NOT NULL DEFAULT 0 COMMENT '优先级(0-普通,1-重要)',
  `read_time` datetime DEFAULT NULL COMMENT '阅读时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除(0-未删除,1-已删除)',
  PRIMARY KEY (`id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';

-- 内容管理表
CREATE TABLE IF NOT EXISTS `content_management` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '内容ID',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `category` varchar(30) NOT NULL COMMENT '分类(FRESHMAN_GUIDE-新生报到指引,LEARNING_RESOURCE-在线学习资源,CAMPUS_LIFE-校园生活百科,SCHOOL_RULES-校规校纪)',
  `summary` varchar(500) DEFAULT NULL COMMENT '摘要',
  `content` text COMMENT '内容详情',
  `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图片',
  `tags` varchar(255) DEFAULT NULL COMMENT '标签',
  `author` varchar(50) DEFAULT NULL COMMENT '作者',
  `view_count` int DEFAULT 0 COMMENT '浏览次数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(0-草稿,1-已发布)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容管理表';

-- 活动管理表
CREATE TABLE IF NOT EXISTS `activity_management` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `title` varchar(200) NOT NULL COMMENT '活动标题',
  `type` varchar(30) DEFAULT NULL COMMENT '类型(LECTURE-讲座,COMPETITION-竞赛,CULTURAL-文化活动,SPORTS-体育活动)',
  `description` varchar(500) DEFAULT NULL COMMENT '活动描述',
  `location` varchar(200) DEFAULT NULL COMMENT '地点',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `registration_deadline` datetime DEFAULT NULL COMMENT '报名截止时间',
  `max_participants` int DEFAULT 0 COMMENT '最大人数',
  `current_participants` int DEFAULT 0 COMMENT '当前人数',
  `requirements` text COMMENT '活动要求',
  `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图',
  `organizer` varchar(100) DEFAULT NULL COMMENT '主办方',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(0-草稿,1-已发布,2-已结束)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动管理表';

-- 活动报名表
CREATE TABLE IF NOT EXISTS `activity_registration` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '报名ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `user_name` varchar(50) DEFAULT NULL COMMENT '用户姓名',
  `student_id` varchar(30) DEFAULT NULL COMMENT '学号',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态(0-待审核,1-已通过,2-已拒绝)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_activity_id` (`activity_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动报名表';

-- 校园资讯表
CREATE TABLE IF NOT EXISTS `news_management` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '资讯ID',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `category` varchar(30) NOT NULL COMMENT '分类(SCHOOL_NEWS-学校新闻,NOTICE-通知公告,ACADEMIC-学术动态,HOT_TOPIC-热点话题)',
  `summary` varchar(500) DEFAULT NULL COMMENT '摘要',
  `content` text COMMENT '内容',
  `author` varchar(50) DEFAULT NULL COMMENT '作者',
  `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图',
  `is_top` tinyint NOT NULL DEFAULT 0 COMMENT '是否置顶(0-否,1-是)',
  `view_count` int DEFAULT 0 COMMENT '浏览次数',
  `like_count` int DEFAULT 0 COMMENT '点赞次数',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(0-草稿,1-已发布)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`),
  KEY `idx_is_top` (`is_top`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校园资讯表';

-- 智能问答语料库表
CREATE TABLE IF NOT EXISTS `qa_corpus` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '语料ID',
  `question` varchar(500) NOT NULL COMMENT '问题',
  `answer` text NOT NULL COMMENT '答案',
  `category` varchar(30) NOT NULL COMMENT '分类',
  `keywords` varchar(255) DEFAULT NULL COMMENT '关键词',
  `intent` varchar(50) DEFAULT NULL COMMENT '意图标签(用于Rasa训练)',
  `confidence_threshold` decimal(3,2) DEFAULT 0.70 COMMENT '置信度阈值',
  `source_type` varchar(20) DEFAULT NULL COMMENT '来源类型(CONTENT-内容,ACTIVITY-活动,NEWS-资讯,MANUAL-手动)',
  `source_id` bigint DEFAULT NULL COMMENT '来源ID',
  `usage_count` int DEFAULT 0 COMMENT '使用次数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(0-禁用,1-启用)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问答语料库表';

-- 问答对话记录表
CREATE TABLE IF NOT EXISTS `qa_conversation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '对话ID',
  `session_id` varchar(100) NOT NULL COMMENT '会话ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `user_question` varchar(500) NOT NULL COMMENT '用户问题',
  `bot_answer` text COMMENT '机器人回答',
  `intent` varchar(50) DEFAULT NULL COMMENT '识别的意图',
  `confidence` decimal(3,2) DEFAULT NULL COMMENT '置信度',
  `corpus_id` bigint DEFAULT NULL COMMENT '匹配的语料ID',
  `satisfaction` tinyint DEFAULT NULL COMMENT '用户满意度(1-5星)',
  `feedback` varchar(500) DEFAULT NULL COMMENT '用户反馈',
  `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_corpus_id` (`corpus_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问答对话记录表';

-- 客服会话表
CREATE TABLE IF NOT EXISTS `customer_service_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `student_name` varchar(50) NOT NULL COMMENT '学生用户名',
  `service_id` bigint DEFAULT NULL COMMENT '客服ID(管理员ID)',
  `service_name` varchar(50) DEFAULT NULL COMMENT '客服用户名',
  `status` varchar(20) NOT NULL DEFAULT 'waiting' COMMENT '会话状态(waiting-等待中,serving-服务中,closed-已关闭)',
  `unread_count` int NOT NULL DEFAULT 0 COMMENT '未读消息数',
  `last_message` text COMMENT '最后一条消息内容',
  `last_message_time` datetime DEFAULT NULL COMMENT '最后消息时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_service_id` (`service_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客服会话表';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS `chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `session_id` bigint NOT NULL COMMENT '会话ID',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `sender_name` varchar(50) NOT NULL COMMENT '发送者用户名',
  `sender_role` varchar(20) NOT NULL COMMENT '发送者角色(student/admin)',
  `receiver_id` bigint DEFAULT NULL COMMENT '接收者ID',
  `content` text NOT NULL COMMENT '消息内容',
  `message_type` varchar(20) NOT NULL DEFAULT 'text' COMMENT '消息类型(text/image/file)',
  `is_read` tinyint NOT NULL DEFAULT 0 COMMENT '是否已读(0-未读,1-已读)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- 问题反馈表
CREATE TABLE IF NOT EXISTS `feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '反馈ID',
  `user_id` bigint NOT NULL COMMENT '反馈用户ID',
  `username` varchar(50) NOT NULL COMMENT '反馈用户名',
  `feedback_type` varchar(30) NOT NULL COMMENT '反馈类型(功能问题/内容建议/其他)',
  `title` varchar(200) NOT NULL COMMENT '反馈标题',
  `content` text NOT NULL COMMENT '反馈内容',
  `contact` varchar(100) DEFAULT NULL COMMENT '联系方式(手机号或邮箱)',
  `status` varchar(20) NOT NULL DEFAULT 'pending' COMMENT '处理状态(pending-待处理,processing-处理中,resolved-已解决,rejected-已驳回)',
  `handler_id` bigint DEFAULT NULL COMMENT '处理人ID',
  `handler_name` varchar(50) DEFAULT NULL COMMENT '处理人用户名',
  `reply` text COMMENT '回复内容',
  `reply_time` datetime DEFAULT NULL COMMENT '回复时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_feedback_type` (`feedback_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题反馈表';

-- ========================================
-- 2. 插入真实初始数据
-- ========================================

-- 用户数据（所有用户初始密码均为：admin123）
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `student_id`, `role`, `gender`, `phone`, `email`, `department`, `major`, `status`) VALUES 
('admin', '$2a$10$fD4uP4pHnlZ827WXheDrV.HLzo/dVW5itVCpbp/20ULXVwBOMJGAC', '系统管理员', 'ADMIN001', 'ADMIN', 1, '13800138000', 'admin@campus.edu.cn', '信息技术中心', '系统管理', 1),
('student01', '$2a$10$fD4uP4pHnlZ827WXheDrV.HLzo/dVW5itVCpbp/20ULXVwBOMJGAC', '张三', '2024001001', 'STUDENT', 1, '13700001001', 'zhangsan@student.edu.cn', '计算机学院', '计算机科学与技术', 1),
('student02', '$2a$10$fD4uP4pHnlZ827WXheDrV.HLzo/dVW5itVCpbp/20ULXVwBOMJGAC', '李四', '2024001002', 'STUDENT', 2, '13700001002', 'lisi@student.edu.cn', '计算机学院', '软件工程', 1),
('student03', '$2a$10$fD4uP4pHnlZ827WXheDrV.HLzo/dVW5itVCpbp/20ULXVwBOMJGAC', '王五', '2024002001', 'STUDENT', 1, '13700002001', 'wangwu@student.edu.cn', '经济管理学院', '工商管理', 1),
('student04', '$2a$10$fD4uP4pHnlZ827WXheDrV.HLzo/dVW5itVCpbp/20ULXVwBOMJGAC', '赵六', '2024002002', 'STUDENT', 2, '13700002002', 'zhaoliu@student.edu.cn', '经济管理学院', '会计学', 1),
('student05', '$2a$10$fD4uP4pHnlZ827WXheDrV.HLzo/dVW5itVCpbp/20ULXVwBOMJGAC', '孙七', '2024003001', 'STUDENT', 1, '13700003001', 'sunqi@student.edu.cn', '外国语学院', '英语', 1);

-- 内容管理数据
INSERT INTO `content_management` (`title`, `category`, `summary`, `content`, `tags`, `author`, `view_count`, `status`) VALUES 
('新生报到流程详解', 'FRESHMAN_GUIDE', '详细介绍新生报到的完整流程和注意事项', '新生报到流程：\n1. 到达学校后先前往新生接待处报到\n2. 凭录取通知书和身份证办理报到手续\n3. 领取校园卡、宿舍钥匙、新生资料袋\n4. 前往指定宿舍楼办理入住\n5. 完成缴费（可现场缴费或提前网上缴费）\n6. 参加新生入学教育和军训\n\n注意事项：\n- 请携带录取通知书、身份证原件及复印件、户口迁移证（如需迁户口）\n- 一寸照片8张、党团组织关系材料\n- 家庭经济困难学生可申请绿色通道\n- 建议提前准备好学费和住宿费', '新生,报到,流程', '教务处', 156, 1),
('新生入学必备物品清单', 'FRESHMAN_GUIDE', '为新生整理的入学必备物品清单', '入学必备物品清单：\n\n必带证件类：\n- 录取通知书\n- 身份证及复印件（10份）\n- 户口本复印件\n- 一寸照片（10张）\n- 团员证/党员材料\n\n生活用品类：\n- 床上用品（学校统一购买或自带）\n- 洗漱用品、毛巾、水杯\n- 换洗衣物、衣架\n- 常用药品（感冒药、肠胃药等）\n\n学习用品类：\n- 笔记本电脑及充电器\n- 文具用品\n- 书包或背包\n\n其他物品：\n- 充电宝、数据线\n- 雨伞、运动鞋\n- 少量现金（500-1000元）', '新生,物品清单,准备', '学工处', 203, 1),
('校园卡使用指南', 'FRESHMAN_GUIDE', '校园卡的办理、使用和充值说明', '校园卡使用指南：\n\n一、校园卡功能：\n1. 食堂就餐消费\n2. 超市购物\n3. 图书馆借书\n4. 门禁出入\n5. 宿舍热水\n6. 洗衣机使用\n\n二、充值方式：\n1. 食堂充值点现金充值\n2. 微信公众号"校园卡服务"在线充值\n3. 支付宝"校园生活"充值\n4. 自助充值机充值\n\n三、挂失与补办：\n- 丢失后立即拨打服务电话：0510-12345678挂失\n- 携带身份证到行政楼一楼校园卡中心补办\n- 补办工本费：20元\n\n四、注意事项：\n- 请妥善保管，避免折损\n- 余额不足时及时充值\n- 不要将卡借给他人使用', '校园卡,充值,使用指南', '后勤处', 445, 1),
('图书馆电子资源使用教程', 'LEARNING_RESOURCE', '介绍图书馆各类电子资源的访问和使用方法', '图书馆电子资源使用教程：\n\n一、访问方式：\n1. 校内访问：连接校园网后直接访问图书馆主页\n2. 校外访问：通过VPN系统远程访问\n\n二、主要数据库：\n1. 中国知网（CNKI）- 中文期刊、学位论文\n2. 万方数据库 - 学术期刊、会议论文\n3. 维普数据库 - 中文科技期刊\n4. Web of Science - 外文期刊检索\n5. IEEE Xplore - 电气电子类文献\n6. SpringerLink - 外文图书和期刊\n\n三、文献检索技巧：\n- 使用关键词组合检索\n- 利用高级检索功能\n- 设置学科分类和时间范围\n- 善用引文追踪功能\n\n四、使用时间：\n全天24小时开放\n\n五、技术支持：\n咨询电话：0510-85678901\n邮箱：library@campus.edu.cn', '图书馆,电子资源,数据库', '图书馆', 567, 1),
('在线学习平台使用说明', 'LEARNING_RESOURCE', '学校在线学习平台的注册、登录和课程学习指南', '在线学习平台使用说明：\n\n一、平台介绍：\n学校采用"智慧课堂"在线学习平台，支持课程学习、作业提交、在线考试等功能。\n\n二、登录方式：\n网址：https://elearning.campus.edu.cn\n账号：学号\n初始密码：身份证后6位\n\n三、主要功能：\n1. 课程学习 - 观看视频、阅读资料\n2. 作业提交 - 完成并提交作业\n3. 在线测试 - 参加章节测验和期末考试\n4. 讨论互动 - 参与课程讨论区\n5. 学习统计 - 查看学习进度和成绩\n\n四、学习建议：\n- 按时完成课程视频学习\n- 认真完成课后作业\n- 积极参与讨论\n- 注意作业提交截止时间\n\n五、技术支持：\n客服电话：0510-85678902\n在线客服：平台右下角"在线咨询"', '在线学习,网课,平台', '教务处', 389, 1),
('公选课选课指南', 'LEARNING_RESOURCE', '公共选修课的选课时间、流程和注意事项', '公选课选课指南：\n\n一、选课时间：\n每学期第2周周一至周三\n上午9:00-晚上21:00\n\n二、选课流程：\n1. 登录教务系统（http://jwc.campus.edu.cn）\n2. 点击"网上选课"\n3. 选择"公共选修课"\n4. 浏览课程列表，查看课程介绍\n5. 点击"选定"按钮\n6. 确认选课结果\n\n三、选课要求：\n- 每学期最多选3门\n- 毕业前至少修满8学分\n- 艺术类课程至少2学分\n- 创新创业类课程至少2学分\n\n四、注意事项：\n- 选课采用先到先得原则\n- 请确认上课时间不冲突\n- 选课成功后不得随意退课\n- 无故缺课3次以上成绩记为不及格\n\n五、课程类型：\n- 人文社科类\n- 自然科学类\n- 艺术修养类\n- 创新创业类\n- 体育健康类', '选课,公选课,教务', '教务处', 521, 1),
('学生食堂就餐指南', 'CAMPUS_LIFE', '介绍校内各个食堂的位置、特色和就餐时间', '学生食堂就餐指南：\n\n一、食堂分布：\n1. 第一食堂 - 东校区，3层\n2. 第二食堂 - 西校区，2层  \n3. 第三食堂 - 南校区，2层\n4. 清真食堂 - 东校区第一食堂3楼\n\n二、就餐时间：\n早餐：6:30-9:00\n午餐：11:00-13:30\n晚餐：17:00-19:30\n夜宵：20:00-22:00（第一、二食堂）\n\n三、特色美食：\n第一食堂：麻辣烫、砂锅、盖浇饭\n第二食堂：面食、水饺、煎饼果子\n第三食堂：各地特色菜、小吃\n\n四、支付方式：\n- 校园卡（优惠5%）\n- 微信支付\n- 支付宝\n\n五、价格参考：\n早餐：3-8元\n午餐/晚餐：8-15元\n\n六、就餐建议：\n- 错峰就餐，避开高峰期\n- 注意节约粮食\n- 用餐后请将餐具送至回收处', '食堂,就餐,美食', '后勤处', 892, 1),
('校内医疗服务指南', 'CAMPUS_LIFE', '校医院的位置、服务时间和就医流程', '校内医疗服务指南：\n\n一、校医院位置：\n东校区行政楼西侧100米\n\n二、服务时间：\n周一至周五：8:00-11:30, 14:00-17:30\n周末及节假日：值班制（9:00-16:00）\n24小时急诊电话：0510-85678999\n\n三、服务项目：\n1. 内科、外科门诊\n2. 常见病诊疗\n3. 预防接种\n4. 健康体检\n5. 医保报销\n\n四、就医流程：\n1. 携带校园卡和医保卡\n2. 挂号（校园卡挂号免费）\n3. 就诊\n4. 缴费（可用医保）\n5. 取药\n\n五、医保报销：\n- 门诊报销比例：70%\n- 住院报销比例：85%\n- 年度限额：20000元\n\n六、常备药品：\n校医院药房提供常用药品，价格低于市场价\n\n七、紧急情况：\n如遇紧急情况，请拨打120或校医院急诊电话', '校医院,医疗,就医', '校医院', 334, 1),
('校园网使用说明', 'CAMPUS_LIFE', '校园网的开通、使用和故障处理', '校园网使用说明：\n\n一、开通方式：\n1. 宿舍有线网络：入学时自动开通\n2. 无线网络：下载"校园网客户端"APP登录\n\n二、账号密码：\n账号：学号\n初始密码：身份证后6位\n\n三、无线网络：\nSSID：Campus-WiFi\n覆盖区域：教学楼、图书馆、食堂、宿舍\n\n四、网络套餐：\n1. 基础套餐：20元/月，100GB流量\n2. 标准套餐：30元/月，200GB流量  \n3. 无限套餐：50元/月，不限流量\n\n五、缴费方式：\n- 校园网客户端APP在线缴费\n- 微信公众号"智慧校园"缴费\n- 网络信息中心现场缴费\n\n六、常见问题：\n1. 忘记密码：凭身份证到网络中心重置\n2. 无法连接：检查账号余额，重启设备\n3. 网速慢：避开使用高峰期\n\n七、技术支持：\n电话：0510-85678910\n地点：图书馆A座一楼网络信息中心\n服务时间：周一至周五 8:00-17:30', '校园网,网络,WiFi', '网络中心', 667, 1),
('学生考试纪律规定', 'SCHOOL_RULES', '考试纪律要求和违纪处理办法', '学生考试纪律规定：\n\n一、考前准备：\n1. 提前15分钟到达考场\n2. 携带学生证、身份证\n3. 准备考试用品（透明笔袋）\n4. 禁止携带手机等电子设备\n\n二、考试纪律：\n1. 服从监考教师安排\n2. 按指定座位就座\n3. 不得交头接耳、左顾右盼\n4. 不得偷看他人试卷\n5. 不得传递答题物品\n6. 不得提前交卷离场（开考30分钟后方可交卷）\n\n三、违纪处理：\n1. 一般违纪：该科成绩记为0分\n2. 作弊行为：记过处分，该科成绩记为0分，取消学位授予资格\n3. 严重作弊：留校察看或开除学籍\n\n四、违纪情形：\n- 携带与考试相关的材料\n- 使用手机等电子设备\n- 夹带小抄\n- 抄袭他人答案\n- 请人代考或替考\n\n五、诚信承诺：\n每位考生需在考前签署《考试诚信承诺书》', '考试,纪律,规定', '教务处', 423, 1),
('学生宿舍管理规定', 'SCHOOL_RULES', '宿舍管理制度和安全规定', '学生宿舍管理规定：\n\n一、作息时间：\n- 早上开门：6:00\n- 晚上关门：23:00\n- 熄灯时间：23:30\n\n二、安全管理：\n1. 禁止使用违规电器（电饭煲、热得快、电磁炉等）\n2. 禁止私拉电线\n3. 禁止使用明火\n4. 禁止存放易燃易爆物品\n5. 离开宿舍关好门窗，切断电源\n\n三、卫生管理：\n1. 保持宿舍整洁\n2. 按时清理垃圾\n3. 每周进行卫生检查\n4. 评选文明宿舍\n\n四、访客管理：\n1. 外来人员需登记\n2. 异性不得进入宿舍\n3. 访客不得留宿\n\n五、纪律要求：\n1. 不得晚归或夜不归宿\n2. 不得酗酒闹事\n3. 不得饲养宠物\n4. 不得进行商业活动\n\n六、违规处理：\n- 使用违规电器：没收电器，警告处分\n- 夜不归宿：通报批评\n- 多次违规：记过处分\n- 严重违规：取消住宿资格', '宿舍,管理,规定', '学工处', 556, 1);

-- 活动管理数据
INSERT INTO `activity_management` (`title`, `type`, `description`, `location`, `start_time`, `end_time`, `registration_deadline`, `max_participants`, `current_participants`, `requirements`, `cover_image`, `organizer`, `status`) VALUES 
('2024年校园篮球联赛', 'SPORTS', '一年一度的校园篮球联赛即将开始，欢迎各学院组队参加。比赛采用小组循环赛+淘汰赛制，冠军队伍将获得奖金和荣誉证书。', '东校区体育馆', '2024-11-10 14:00:00', '2024-11-24 18:00:00', '2024-11-05 17:00:00', 200, 85, '1. 以学院为单位组队，每队10-12人\n2. 参赛队员须为本校在读学生\n3. 所有队员需购买保险\n4. 遵守比赛规则和体育道德', 'https://picsum.photos/300/200?random=1', '体育部', 1),
('人工智能前沿技术讲座', 'LECTURE', '邀请清华大学人工智能研究院张教授主讲《大模型时代的机遇与挑战》，深入探讨GPT、BERT等大模型技术及其应用前景。', '学术报告厅A101', '2024-10-28 15:00:00', '2024-10-28 17:00:00', '2024-10-27 12:00:00', 300, 178, '1. 面向全校师生开放\n2. 请提前10分钟入场\n3. 会场内请保持安静\n4. 讲座结束后设有提问环节', 'https://picsum.photos/300/200?random=2', '计算机学院', 1),
('第十届程序设计大赛', 'COMPETITION', '校级程序设计竞赛，设置算法题、工程题两个赛道。前三名可获得奖金和证书，并有机会代表学校参加省级比赛。', '计算机实验楼机房', '2024-11-15 09:00:00', '2024-11-15 17:00:00', '2024-11-10 17:00:00', 150, 67, '1. 本校在读本科生、研究生均可报名\n2. 个人参赛，不得组队\n3. 可携带纸质资料\n4. 不得使用网络查询\n5. 熟悉C/C++/Java/Python任一语言', 'https://picsum.photos/300/200?random=3', '计算机学院', 1),
('迎新晚会节目征集', 'CULTURAL', '2024年迎新晚会节目征集开始！欢迎各类才艺展示：歌舞、小品、相声、器乐演奏、魔术等。优秀节目将在迎新晚会上演出。', '大学生活动中心', '2024-11-20 19:00:00', '2024-11-20 21:30:00', '2024-11-01 17:00:00', 100, 42, '1. 个人或团体均可报名\n2. 节目时长控制在5分钟以内\n3. 需提前进行节目审核和彩排\n4. 自备服装和道具\n5. 提交节目视频至邮箱：art@campus.edu.cn', 'https://picsum.photos/300/200?random=4', '学生会文艺部', 1),
('创新创业大赛宣讲会', 'LECTURE', '第十届"互联网+"大学生创新创业大赛校内选拔赛宣讲会，介绍大赛规则、参赛流程、项目申报等内容，并分享往届优秀案例。', '图书馆报告厅', '2024-10-25 14:30:00', '2024-10-25 16:30:00', '2024-10-24 17:00:00', 200, 93, '1. 有创业意向的在校学生\n2. 建议组队参加\n3. 请携带笔记本电脑\n4. 会后可现场咨询', 'https://picsum.photos/300/200?random=5', '创新创业学院', 1),
('秋季校园马拉松', 'SPORTS', '金秋时节，邀你一起奔跑！设置5公里健康跑和10公里挑战跑两个项目，完赛者获得纪念奖牌和完赛证书。', '校园环形道路', '2024-11-03 07:30:00', '2024-11-03 11:00:00', '2024-10-30 17:00:00', 500, 312, '1. 身体健康，无心脏病等不适合剧烈运动的疾病\n2. 自愿购买意外保险\n3. 穿着运动装备和跑鞋\n4. 按照指定路线跑完全程', 'https://picsum.photos/300/200?random=6', '体育部', 1);

-- 校园资讯数据
INSERT INTO `news_management` (`title`, `category`, `summary`, `content`, `author`, `cover_image`, `is_top`, `view_count`, `like_count`, `publish_time`, `status`) VALUES 
('我校在2024年全国大学生数学建模竞赛中获得优异成绩', 'SCHOOL_NEWS', '在刚刚结束的2024年全国大学生数学建模竞赛中，我校代表队荣获国家一等奖2项、二等奖5项，获奖数量创历史新高。', '2024年全国大学生数学建模竞赛成绩于10月20日公布，我校共有15支队伍参赛，最终获得国家一等奖2项、国家二等奖5项、省级一等奖3项、省级二等奖5项的优异成绩。\n\n其中，由计算机学院张三、李四、王五组成的参赛队伍以出色的建模能力和创新思维荣获国家一等奖，他们的作品《基于深度学习的交通流量预测模型》得到了评委的高度评价。\n\n据悉，全国大学生数学建模竞赛是教育部认可的大学生学科竞赛之一，每年吸引全国近1500所高校、超过5万支队伍参赛，竞争十分激烈。\n\n校领导对获奖同学表示祝贺，并鼓励同学们继续努力，在学术研究和科技创新道路上取得更大成就。', '教务处', 'https://picsum.photos/400/250?random=11', 1, 1523, 89, '2024-10-21 09:00:00', 1),
('关于2024-2025学年第一学期期末考试安排的通知', 'NOTICE', '根据校历安排，本学期期末考试定于第18-19周进行，现将有关事项通知如下。', '各学院、全体师生：\n\n根据学校校历安排，2024-2025学年第一学期期末考试定于2025年1月6日至1月17日（第18-19周）进行。现将有关事项通知如下：\n\n一、考试时间安排\n1. 第18周（1月6日-1月10日）：公共必修课考试\n2. 第19周（1月13日-1月17日）：专业课考试\n3. 具体考试时间和地点详见教务系统\n\n二、考试纪律要求\n1. 考生须携带学生证和身份证参加考试\n2. 提前15分钟到达考场\n3. 严禁携带手机等电子设备进入考场\n4. 严格遵守考试纪律，诚信考试\n\n三、成绩查询\n考试结束后2周内公布成绩，请登录教务系统查询\n\n四、补考安排\n下学期开学第2周进行补考，具体安排另行通知\n\n请各学院认真组织，确保考试工作顺利进行。\n\n特此通知。\n\n教务处\n2024年10月18日', '教务处', NULL, 1, 2341, 45, '2024-10-18 10:30:00', 1),
('著名经济学家李教授做客我校"名家讲坛"', 'ACADEMIC', '10月15日下午，著名经济学家、北京大学李教授应邀来校，在学术报告厅为师生作了题为《数字经济时代的机遇与挑战》的学术报告。', '10月15日下午，著名经济学家、北京大学经济学院李教授应邀来校，在学术报告厅为师生作了题为《数字经济时代的机遇与挑战》的学术报告。校党委副书记、校长出席报告会，经济管理学院师生500余人聆听了报告。\n\n报告中，李教授从数字经济的定义和特征入手，深入分析了数字经济对传统产业的影响，阐述了数字技术如何重塑商业模式和经济结构。他指出，数字经济已成为推动经济高质量发展的重要引擎，大数据、人工智能、区块链等新技术的应用为各行各业带来了新的发展机遇。\n\n李教授还结合国内外案例，分享了数字化转型的成功经验和面临的挑战。他强调，要抓住数字经济发展的战略机遇期，加强数字技术人才培养，推动产学研深度融合。\n\n报告会后，李教授与师生进行了互动交流，耐心解答了大家提出的问题。\n\n此次报告会是我校"名家讲坛"系列活动之一，旨在拓宽师生学术视野，激发创新思维。', '经管学院', 'https://picsum.photos/400/250?random=12', 0, 876, 67, '2024-10-16 08:30:00', 1),
('2024年国家奖学金评选结果公示', 'NOTICE', '根据《国家奖学金评审办法》，经个人申请、学院推荐、学校评审，拟推荐以下同学获得2024年国家奖学金，现予以公示。', '根据《国家奖学金评审办法》和《学生奖学金评定管理办法》，经个人申请、学院推荐、学校奖学金评审委员会评审，拟推荐以下同学获得2024年国家奖学金，现予以公示。\n\n公示名单（共50人）：\n\n计算机学院（8人）：\n张三（计算机2021级）、李四（软件工程2021级）...\n\n经济管理学院（6人）：\n王五（工商管理2021级）、赵六（会计学2022级）...\n\n外国语学院（5人）：\n...\n\n公示时间：2024年10月10日至10月14日\n\n公示期间，如对评选结果有异议，可向学生工作处反映。\n联系电话：0510-85678920\n邮箱：xsc@campus.edu.cn\n\n学生工作处\n2024年10月10日', '学工处', NULL, 1, 1987, 123, '2024-10-10 09:00:00', 1),
('图书馆新增电子图书数据库试用通知', 'HOT_TOPIC', '为满足师生科研和学习需求，图书馆新增3个电子图书数据库，现开通试用，欢迎广大师生使用。', '各位师生：\n\n为进一步丰富图书馆电子资源，满足师生科研和学习需求，图书馆新增以下3个电子图书数据库，现开通试用，欢迎广大师生使用。\n\n一、试用数据库\n1. "读秀"学术搜索\n- 内容：中文图书、期刊、报纸等文献资源\n- 特色：支持全文检索、文献传递\n\n2. "超星电子图书"\n- 内容：涵盖各学科的电子图书100万余种\n- 特色：支持在线阅读和下载\n\n3. "智慧树在线教育平台"\n- 内容：各类精品在线课程3000余门\n- 特色：支持在线学习、获取学分\n\n二、试用时间\n2024年10月20日至2025年1月20日\n\n三、访问方式\n1. 校内访问：连接校园网后，登录图书馆主页点击相应数据库\n2. 校外访问：通过VPN系统远程访问\n\n四、使用反馈\n试用期间，欢迎师生反馈使用体验和建议。\n联系方式：library@campus.edu.cn\n电话：0510-85678901\n\n图书馆\n2024年10月19日', '图书馆', NULL, 0, 654, 34, '2024-10-19 15:00:00', 1),
('学校举办2024年秋季校园招聘会', 'SCHOOL_NEWS', '10月22日，我校2024年秋季校园招聘会在体育馆举行，来自全国各地的200余家企业参会，提供就业岗位5000余个。', '10月22日，我校2024年秋季校园招聘会在东校区体育馆举行。本次招聘会吸引了来自北京、上海、深圳、杭州等地的200余家知名企业参会，涵盖IT互联网、金融、制造、教育、医疗等多个行业，提供就业岗位5000余个。\n\n招聘会现场人头攒动，应届毕业生们身着正装，手持简历，在各个企业展位前咨询岗位信息、投递简历。华为、阿里巴巴、腾讯、字节跳动等知名企业的展位前排起了长队。\n\n计算机学院2024届毕业生张同学表示："今天投了8份简历，有3家企业当场给了我面试邀约，感觉收获很大。"经管学院的李同学说："企业提供的岗位和薪资待遇都不错，让我对就业更有信心了。"\n\n学校就业指导中心负责人介绍，为帮助毕业生更好地就业，学校提前一个月就开始筹备此次招聘会，精心挑选优质企业，并为学生提供简历指导、面试技巧等就业服务。后续还将举办多场专场招聘会和宣讲会。\n\n截至当天下午5点，共有3000余名学生参加招聘会，收到简历8000余份，现场达成就业意向300余人。', '就业指导中心', 'https://picsum.photos/400/250?random=13', 0, 1234, 78, '2024-10-23 10:00:00', 1);

-- 智能问答语料库数据
INSERT INTO `qa_corpus` (`question`, `answer`, `category`, `keywords`, `intent`, `confidence_threshold`, `source_type`, `source_id`, `status`) VALUES 
('新生报到需要带什么材料？', '新生报到需要携带以下材料：\n1. 录取通知书\n2. 身份证原件及复印件\n3. 户口迁移证（如需迁户口）\n4. 一寸照片8张\n5. 党团组织关系材料\n6. 高中档案\n建议您提前准备好这些材料，以便顺利办理报到手续。', 'FRESHMAN', '新生,报到,材料', 'freshman_materials', 0.75, 'CONTENT', 1, 1),
('新生报到流程是什么？', '新生报到流程如下：\n1. 到新生接待处报到\n2. 凭录取通知书和身份证办理手续\n3. 领取校园卡、宿舍钥匙、资料袋\n4. 前往宿舍办理入住\n5. 完成学费缴纳\n6. 参加新生入学教育\n整个流程大约需要2-3小时，建议提前规划好时间。', 'FRESHMAN', '新生,报到,流程', 'freshman_process', 0.75, 'CONTENT', 1, 1),
('校园卡怎么充值？', '校园卡充值方式：\n1. 食堂充值点现金充值\n2. 微信公众号"校园卡服务"在线充值\n3. 支付宝"校园生活"充值\n4. 自助充值机充值\n建议您绑定微信或支付宝，方便随时充值。', 'CAMPUS_LIFE', '校园卡,充值', 'card_recharge', 0.70, 'CONTENT', 3, 1),
('校园卡丢了怎么办？', '校园卡丢失后请立即：\n1. 拨打服务电话0510-12345678挂失\n2. 携带身份证到行政楼一楼校园卡中心补办\n3. 补办工本费20元\n挂失后原卡自动失效，余额会转入新卡。', 'CAMPUS_LIFE', '校园卡,丢失,补办', 'card_lost', 0.70, 'CONTENT', 3, 1),
('图书馆开放时间是什么时候？', '图书馆开放时间：\n周一至周日：7:00-22:30\n节假日：8:00-21:00\n自习室24小时开放\n电子阅览室：8:00-22:00', 'LEARNING', '图书馆,开放时间', 'library_hours', 0.75, 'MANUAL', NULL, 1),
('图书馆怎么借书？', '图书馆借书流程：\n1. 在检索机上查找图书位置\n2. 到相应书架找到图书\n3. 携带图书和校园卡到自助借还机\n4. 按提示完成借书操作\n借期：30天，可续借2次，每次15天。', 'LEARNING', '图书馆,借书', 'library_borrow', 0.70, 'MANUAL', NULL, 1),
('如何访问图书馆电子资源？', '访问图书馆电子资源的方法：\n1. 校内访问：连接校园网后直接访问图书馆主页\n2. 校外访问：通过VPN系统远程访问\n主要数据库包括知网、万方、维普、Web of Science等。', 'LEARNING', '图书馆,电子资源,数据库', 'library_eresource', 0.70, 'CONTENT', 4, 1),
('如何选课？', '选课流程：\n1. 登录教务系统（http://jwc.campus.edu.cn）\n2. 点击"网上选课"\n3. 选择课程类别\n4. 浏览课程列表\n5. 点击"选定"按钮\n6. 确认选课结果\n注意：选课采用先到先得原则，请在规定时间内完成。', 'ACADEMIC', '选课,教务', 'course_selection', 0.75, 'CONTENT', 6, 1),
('期末考试什么时候？', '本学期期末考试时间：\n第18周（1月6日-1月10日）：公共必修课\n第19周（1月13日-1月17日）：专业课\n具体考试时间和地点请登录教务系统查询。', 'ACADEMIC', '期末考试,考试时间', 'exam_schedule', 0.75, 'NEWS', 2, 1),
('如何查成绩？', '成绩查询方法：\n1. 登录教务系统\n2. 点击"成绩查询"\n3. 选择学期\n4. 查看各科成绩\n考试结束后2周内公布成绩。', 'ACADEMIC', '成绩,查询', 'grade_inquiry', 0.70, 'MANUAL', NULL, 1),
('食堂在哪里？', '学校有3个食堂：\n1. 第一食堂 - 东校区，3层\n2. 第二食堂 - 西校区，2层\n3. 第三食堂 - 南校区，2层\n另有清真食堂在第一食堂3楼。', 'CAMPUS_LIFE', '食堂,位置', 'canteen_location', 0.75, 'CONTENT', 8, 1),
('食堂几点开门？', '食堂开放时间：\n早餐：6:30-9:00\n午餐：11:00-13:30\n晚餐：17:00-19:30\n夜宵：20:00-22:00（第一、二食堂）', 'CAMPUS_LIFE', '食堂,时间', 'canteen_hours', 0.70, 'CONTENT', 8, 1),
('最近有什么活动？', '近期活动包括：\n1. 校园篮球联赛（11月10日）\n2. 人工智能讲座（10月28日）\n3. 程序设计大赛（11月15日）\n4. 迎新晚会（11月20日）\n详情请查看活动管理页面。', 'ACTIVITY', '活动', 'activity_list', 0.70, 'ACTIVITY', NULL, 1),
('如何报名参加活动？', '活动报名方式：\n1. 登录系统\n2. 进入"活动管理"页面\n3. 选择感兴趣的活动\n4. 点击"立即报名"\n5. 填写报名信息\n6. 提交报名\n请注意报名截止时间。', 'ACTIVITY', '活动,报名', 'activity_register', 0.70, 'MANUAL', NULL, 1),
('校医院在哪里？', '校医院位于东校区行政楼西侧100米处。\n服务时间：周一至周五 8:00-17:30\n急诊电话：0510-85678999（24小时）', 'CAMPUS_LIFE', '校医院,医疗', 'hospital_location', 0.75, 'CONTENT', 9, 1),
('校园网怎么连接？', '校园网连接方式：\n1. 有线网络：插网线自动连接\n2. 无线网络：连接WiFi"Campus-WiFi"，下载"校园网客户端"APP登录\n账号：学号\n初始密码：身份证后6位', 'CAMPUS_LIFE', '校园网,网络,WiFi', 'network_connect', 0.70, 'CONTENT', 10, 1),
('校园网多少钱？', '校园网套餐：\n1. 基础套餐：20元/月，100GB流量\n2. 标准套餐：30元/月，200GB流量\n3. 无限套餐：50元/月，不限流量\n建议根据使用需求选择合适套餐。', 'CAMPUS_LIFE', '校园网,费用,价格', 'network_price', 0.70, 'CONTENT', 10, 1),
('宿舍管理规定有哪些？', '宿舍管理规定：\n1. 作息时间：早上6:00开门，晚上23:00关门，23:30熄灯\n2. 安全管理：禁止使用违规电器、私拉电线、使用明火\n3. 卫生管理：保持宿舍整洁，按时清理垃圾\n4. 访客管理：外来人员需登记，异性不得进入宿舍\n5. 纪律要求：不得晚归或夜不归宿', 'CAMPUS_LIFE', '宿舍,管理,规定', 'dormitory_rules', 0.75, 'CONTENT', 11, 1),
('宿舍可以用什么电器？', '宿舍可以使用的电器：\n✅ 允许使用：台灯、充电器、笔记本电脑、手机、小风扇、电吹风（小功率）\n❌ 禁止使用：电饭煲、热得快、电磁炉、电暖器、电热毯等大功率电器\n违规使用将被没收电器并给予警告处分，请遵守用电规定，注意安全。', 'CAMPUS_LIFE', '宿舍,电器,规定', 'dormitory_appliances', 0.75, 'CONTENT', 11, 1),
('如何请假？', '请假办理流程：\n1. 登录学生管理系统\n2. 填写请假申请表\n3. 说明请假事由、时间\n4. 上传证明材料（病假需医院证明，事假需家长同意书）\n5. 提交给辅导员审批\n6. 批准后方可离校\n注意：请假超过3天需学院领导审批，超过1周需报学生处备案。', 'ACADEMIC', '请假,流程,手续', 'leave_application', 0.75, 'MANUAL', NULL, 1),
('考试纪律有哪些规定？', '考试纪律规定：\n1. 提前15分钟到达考场\n2. 携带学生证、身份证\n3. 禁止携带手机等电子设备\n4. 不得交头接耳、抄袭作弊\n5. 不得提前交卷（开考30分钟后方可交卷）\n违纪处理：作弊者该科成绩记为0分，给予记过处分，严重者将开除学籍。请诚信考试！', 'ACADEMIC', '考试,纪律,规定', 'exam_discipline', 0.75, 'CONTENT', 12, 1),
('如何查询考试成绩？', '成绩查询方法：\n1. 登录教务系统（http://jwc.campus.edu.cn）\n2. 点击"成绩查询"\n3. 选择学期\n4. 查看各科成绩和绩点\n考试结束后2周内公布成绩。如对成绩有疑问，可在成绩公布后1周内申请复查。', 'ACADEMIC', '成绩,查询,查分', 'grade_inquiry', 0.70, 'MANUAL', NULL, 1),
('选课时间是什么时候？', '选课时间安排：\n【必修课选课】每学期第1周\n【公选课选课】每学期第2周周一至周三，9:00-21:00\n选课网址：http://jwc.campus.edu.cn\n注意：选课采用先到先得原则，热门课程可能很快选满，请提前规划并及时选课。', 'ACADEMIC', '选课,时间,流程', 'course_selection', 0.75, 'CONTENT', 8, 1),
('食堂营业时间是几点？', '食堂营业时间：\n🌅 早餐：6:30-9:00\n🌞 午餐：11:00-13:30\n🌙 晚餐：17:00-19:30\n🌃 夜宵：20:00-22:00（第一、二食堂）\n建议错峰就餐，避开11:30-12:30和17:30-18:30的高峰期。', 'CAMPUS_LIFE', '食堂,时间,营业', 'canteen_hours', 0.70, 'CONTENT', 9, 1),
('校园有几个食堂？', '学校共有3个食堂：\n1️⃣ 第一食堂 - 东校区，3层，特色：麻辣烫、砂锅、盖浇饭\n2️⃣ 第二食堂 - 西校区，2层，特色：面食、水饺、煎饼果子\n3️⃣ 第三食堂 - 南校区，2层，特色：各地特色菜、小吃\n另有清真食堂在第一食堂3楼。所有食堂支持校园卡、微信、支付宝支付。', 'CAMPUS_LIFE', '食堂,位置,地点', 'canteen_location', 0.75, 'CONTENT', 9, 1),
('校医院在哪里？怎么就医？', '校医院信息：\n📍 位置：东校区行政楼西侧100米\n⏰ 服务时间：周一至周五 8:00-17:30\n🚨 急诊电话：0510-85678999（24小时）\n就医流程：\n1. 携带校园卡和医保卡\n2. 挂号（免费）\n3. 就诊\n4. 缴费（可用医保，报销70%）\n5. 取药', 'CAMPUS_LIFE', '校医院,医疗,就医', 'hospital_location', 0.75, 'CONTENT', 10, 1),
('如何参加活动报名？', '活动报名方法：\n1. 登录校园智能问答系统\n2. 进入"活动管理"页面\n3. 浏览活动列表\n4. 选择感兴趣的活动\n5. 点击"立即报名"\n6. 填写个人信息和联系方式\n7. 提交报名表\n请注意报名截止时间，报名成功后会收到短信通知。', 'ACTIVITY', '活动,报名,参加', 'activity_register', 0.70, 'MANUAL', NULL, 1),
('我什么时候开学？', '开学时间根据校历安排：\n🎓 秋季学期：通常9月初开学（具体日期见录取通知书）\n🌸 春季学期：通常2月下旬或3月初开学\n新生报到时间会提前在录取通知书中说明，一般比老生早1-2周。请关注学校官网和迎新系统的通知。', 'FRESHMAN', '开学,时间,报到', 'freshman_process', 0.70, 'MANUAL', NULL, 1),
('宿舍几点熄灯？', '宿舍作息时间：\n🌅 早上开门：6:00\n🌙 晚上关门：23:00\n💡 熄灯时间：23:30\n请合理安排作息时间，养成良好的生活习惯。如需晚归，请提前向宿管登记说明情况。', 'CAMPUS_LIFE', '宿舍,熄灯,时间', 'dormitory_rules', 0.70, 'CONTENT', 11, 1),
('图书馆可以带水吗？', '图书馆规定：\n✅ 可以：带密封水杯、文具、笔记本电脑\n❌ 不可以：带食物、饮料（非密封）、大声喧哗\n📱 手机需静音\n💺 座位先到先得，离开超过30分钟视为放弃\n请爱护图书，保持安静，共同营造良好的学习环境。', 'LEARNING', '图书馆,规定,带水', 'library_hours', 0.65, 'MANUAL', NULL, 1);

-- 系统通知数据
INSERT INTO `sys_notification` (`title`, `content`, `type`, `priority`) VALUES 
('欢迎使用校园智能问答系统', '欢迎使用校园智能问答系统！本系统可以为您解答各类校园问题。您可以通过智能问答模块提问，系统会为您提供准确的答案。', 'SYSTEM', 0),
('期末考试通知', '2024-2025学年第一学期期末考试将于第18-19周进行，请同学们提前做好复习准备。', 'SYSTEM', 1),
('图书馆新增数据库', '图书馆新增"读秀"学术搜索、"超星电子图书"等数据库，欢迎师生使用。', 'SYSTEM', 0);

-- ========================================
-- 初始化完成
-- ========================================

