/*
 Navicat Premium Data Transfer

 Source Server         : bishe-project
 Source Server Type    : MySQL
 Source Server Version : 80041
 Source Host           : localhost:3306
 Source Schema         : student_score

 Target Server Type    : MySQL
 Target Server Version : 80041
 File Encoding         : 65001

 Date: 03/03/2026 00:28:30
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for edu_certificate
-- ----------------------------
DROP TABLE IF EXISTS `edu_certificate`;
CREATE TABLE `edu_certificate`  (
  `cert_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '申请流水主键ID',
  `student_id` bigint(0) NOT NULL COMMENT '想开证明的学生(关联edu_student)',
  `cert_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务证单开具分类(中文成绩单/英文成绩单/在校证明)',
  `apply_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '申请创建时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '待处理' COMMENT '跟进处理状态(待处理/已出件)',
  PRIMARY KEY (`cert_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '打通打印下发证明的申请流转总线工单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_certificate
-- ----------------------------
INSERT INTO `edu_certificate` VALUES (16, 1, '中文成绩单', '2026-03-02 21:06:20', '已完成');

-- ----------------------------
-- Table structure for edu_course
-- ----------------------------
DROP TABLE IF EXISTS `edu_course`;
CREATE TABLE `edu_course`  (
  `course_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '课程主键ID',
  `course_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '教务官方课程识别代码(唯一)',
  `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '课程标准名称',
  `credit` decimal(3, 1) NOT NULL COMMENT '课程额定学分',
  `hours` int(0) NULL DEFAULT NULL COMMENT '课程额定总学时',
  `org_id` bigint(0) NOT NULL COMMENT '所属开课院系ID(关联sys_org)',
  `course_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '课程性质(必修/选修等)',
  PRIMARY KEY (`course_id`) USING BTREE,
  UNIQUE INDEX `uk_course_code`(`course_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '教务课程信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_course
-- ----------------------------
INSERT INTO `edu_course` VALUES (1, 'CS101', 'Java程序设计', 4.0, 64, 2, '专业核心课');
INSERT INTO `edu_course` VALUES (2, 'CS102', '数据结构', 4.0, 64, 2, '专业必修课');
INSERT INTO `edu_course` VALUES (3, 'CS103', '数据库系统', 3.0, 48, 2, '专业核心课');
INSERT INTO `edu_course` VALUES (4, 'CS104', '计算机网络', 3.5, 56, 2, '专业必修课');
INSERT INTO `edu_course` VALUES (5, 'CS105', '软件工程概论', 2.0, 32, 2, '专业必修课');
INSERT INTO `edu_course` VALUES (6, 'EC101', '微观经济学', 3.0, 48, 3, '专业必修课');
INSERT INTO `edu_course` VALUES (7, 'EC102', '宏观经济学', 3.0, 48, 3, '专业必修课');
INSERT INTO `edu_course` VALUES (8, 'FI101', '金融学原理', 3.0, 48, 3, '专业核心课');
INSERT INTO `edu_course` VALUES (9, 'AC101', '基础会计学', 3.5, 56, 3, '专业核心课');
INSERT INTO `edu_course` VALUES (10, 'EN101', '综合英语(一)', 4.0, 64, 4, '专业必修课');
INSERT INTO `edu_course` VALUES (11, 'EN102', '英语听力', 2.0, 32, 4, '专业必修课');
INSERT INTO `edu_course` VALUES (12, 'EN103', '翻译理论与实践', 3.0, 48, 4, '专业核心课');
INSERT INTO `edu_course` VALUES (13, 'GE101', '高等数学(上)', 5.0, 80, 2, '公共必修课');

-- ----------------------------
-- Table structure for edu_course_plan
-- ----------------------------
DROP TABLE IF EXISTS `edu_course_plan`;
CREATE TABLE `edu_course_plan`  (
  `plan_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '开课计划实例主键ID',
  `course_id` bigint(0) NOT NULL COMMENT '继承的基础课程ID(关联edu_course)',
  `teacher_id` bigint(0) NOT NULL COMMENT '带班主讲教师ID(关联edu_teacher)',
  `term` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '开课时域标志(如: 2023-2024第一学期)',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '教学班次具体名称(如:软工2021-1)',
  `schedule_time` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '固定修读时间(如:周一1-2节,周三3-4节)',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '课场教学楼与具体教室地(如:教学楼A101)',
  `daily_weight` int(0) NULL DEFAULT 10 COMMENT '平时分占比(0-100)',
  `homework_weight` int(0) NULL DEFAULT 20 COMMENT '作业分占比(0-100)',
  `mid_weight` int(0) NULL DEFAULT 20 COMMENT '期中分占比(0-100)',
  `final_weight` int(0) NULL DEFAULT 50 COMMENT '期末分占比(0-100)',
  `capacity` int(0) NULL DEFAULT 0 COMMENT '限制名额选课开放天花板容量',
  `enrolled` int(0) NULL DEFAULT 0 COMMENT '当前成功锁定被入库选上的学生规模总数',
  `version` int(0) NULL DEFAULT 0 COMMENT '乐观锁版本号',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '进行中' COMMENT '排班排课状态(未开课/进行中/结课)',
  PRIMARY KEY (`plan_id`) USING BTREE,
  INDEX `idx_term`(`term`) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '按学期排布指派名将统帅带团班次单流转表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_course_plan
-- ----------------------------
INSERT INTO `edu_course_plan` VALUES (1, 1, 1, '2025-2026第二学期', 'Java-软工22级', '周一1-2节', '教A101', 10, 20, 20, 50, 60, 58, 1, '进行中');
INSERT INTO `edu_course_plan` VALUES (2, 2, 2, '2025-2026第二学期', '数据结构-大类', '周二3-4节', '教A202', 10, 20, 20, 50, 120, 115, 0, '进行中');
INSERT INTO `edu_course_plan` VALUES (3, 3, 3, '2025-2026第一学期', '数据库-软工班', '周三5-6节', '教B105', 10, 20, 20, 50, 60, 60, 0, '进行中');
INSERT INTO `edu_course_plan` VALUES (4, 4, 10, '2025-2026第一学期', '计网-AI班', '周四1-2节', '教A301', 10, 20, 20, 50, 50, 45, 0, '进行中');
INSERT INTO `edu_course_plan` VALUES (5, 5, 13, '2025-2026第二学期', '软工概论-软工班', '周五7-8节', '教C201', 10, 20, 20, 50, 80, 78, 0, '进行中');
INSERT INTO `edu_course_plan` VALUES (6, 6, 4, '2025-2026第二学期', '微观经济-经管大类', '周一3-4节', '阶梯教室1', 10, 20, 20, 50, 150, 140, 0, '进行中');
INSERT INTO `edu_course_plan` VALUES (7, 7, 5, '2025-2026第一学期', '宏观经济-经管大类', '周二1-2节', '阶梯教室2', 10, 20, 20, 50, 150, 145, 0, '进行中');
INSERT INTO `edu_course_plan` VALUES (8, 8, 6, '2025-2026第二学期', '金融原理-金融班', '周三3-4节', '教D102', 10, 20, 20, 50, 60, 50, 0, '结课');
INSERT INTO `edu_course_plan` VALUES (9, 9, 14, '2025-2026第二学期', '基础会计-会计班', '周四5-6节', '教D205', 10, 20, 20, 50, 60, 55, 0, '进行中');
INSERT INTO `edu_course_plan` VALUES (10, 10, 7, '2025-2026第二学期', '综英-英语班', '周一1-2节', '外语楼101', 10, 20, 20, 50, 40, 38, 0, '结课');
INSERT INTO `edu_course_plan` VALUES (11, 11, 8, '2025-2026第一学期', '听力-外语大类', '周三1-2节', '语音室A', 10, 20, 20, 50, 40, 40, 0, '进行中');
INSERT INTO `edu_course_plan` VALUES (12, 12, 12, '2025-2026第二学期', '翻译理论-翻译班', '周五3-4节', '外语楼205', 10, 20, 20, 50, 40, 35, 0, '结课');
INSERT INTO `edu_course_plan` VALUES (13, 13, 1, '2025-2026第一学期', '高数上-理工大类', '周一/周三', '大阶梯', 10, 20, 20, 50, 200, 198, 0, '结课');
INSERT INTO `edu_course_plan` VALUES (14, 14, 1, '2025-2026第一学期', '大体-全校1组', '周四下午', '东操场', 10, 20, 20, 50, 100, 100, 1, '结课');
INSERT INTO `edu_course_plan` VALUES (15, 15, 1, '2025-2026第一学期', '马原-全校3组', '周五早上', '阶梯教室3', 10, 20, 20, 50, 150, 148, 2, '结课');

-- ----------------------------
-- Table structure for edu_report
-- ----------------------------
DROP TABLE IF EXISTS `edu_report`;
CREATE TABLE `edu_report`  (
  `report_id` bigint(0) NOT NULL AUTO_INCREMENT,
  `report_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `report_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `report_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `frequency` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '手动',
  `plan_id` bigint(0) NULL DEFAULT NULL COMMENT '关联edu_course_plan',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  `creator_id` bigint(0) NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '正常',
  PRIMARY KEY (`report_id`) USING BTREE,
  INDEX `idx_plan_id`(`plan_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_report
-- ----------------------------
INSERT INTO `edu_report` VALUES (1, '高数上期末分析', '理工大类高数成绩透视', '综合报表', '每学期', 13, '2026-03-02 17:41:32', '2026-03-02 17:41:32', 2, '正常');
INSERT INTO `edu_report` VALUES (2, '数据库成绩分析', '软工班数据库成绩分布', '分析报表', '每学期', 3, '2026-03-02 17:41:32', '2026-03-02 17:41:32', 2, '正常');
INSERT INTO `edu_report` VALUES (3, '微观经济挂科预警', '经管大类边缘学生名单', '预警报表', '手动', 6, '2026-03-02 17:41:32', '2026-03-02 17:41:32', 2, '正常');
INSERT INTO `edu_report` VALUES (4, '金融原理质量报告', '金融学专业评估', '综合报表', '每年', 8, '2026-03-02 17:41:32', '2026-03-02 17:41:32', 2, '正常');
INSERT INTO `edu_report` VALUES (5, '综英难度系数分析', '考题难度与得分率', '分析报表', '手动', 10, '2026-03-02 17:41:32', '2026-03-02 17:41:32', 2, '正常');
INSERT INTO `edu_report` VALUES (6, '翻译理论测验汇总', '平时成绩对比', '综合报表', '一次性', 12, '2026-03-02 17:41:32', '2026-03-02 17:41:32', 2, '正常');
INSERT INTO `edu_report` VALUES (7, '大体及格率报告', '体测达标率分析', '分析报表', '每学期', 14, '2026-03-02 17:41:32', '2026-03-02 17:41:32', 2, '正常');
INSERT INTO `edu_report` VALUES (8, '马原基础数据', '思政课程概览', '综合报表', '每学期', 15, '2026-03-02 17:41:32', '2026-03-02 17:41:32', 2, '正常');
INSERT INTO `edu_report` VALUES (9, '全校重修筛选单', '集中展示需重修名单', '预警报表', '每学期', NULL, '2026-03-02 17:41:32', '2026-03-02 17:41:32', 2, '正常');
INSERT INTO `edu_report` VALUES (10, '奖学金评定表', '绩点排名前10%', '分析报表', '每年', NULL, '2026-03-02 17:41:32', '2026-03-02 17:41:32', 2, '正常');
INSERT INTO `edu_report` VALUES (11, '软工培养质量监控', '专业核心课达成度', '综合报表', '每年', NULL, '2026-03-02 17:41:32', '2026-03-02 17:41:32', 2, '正常');
INSERT INTO `edu_report` VALUES (12, '英语四级预测模型', '综英与四级通过率', '预测报表', '每年', 10, '2026-03-02 17:41:32', '2026-03-02 17:41:32', 2, '正常');
INSERT INTO `edu_report` VALUES (13, '考勤异常监控报告', '日常缺勤人员统计', '预警报表', '每月', NULL, '2026-03-02 17:41:32', '2026-03-02 17:41:32', 2, '正常');
INSERT INTO `edu_report` VALUES (14, '期中考试总结汇报', '全院期中情况简报', '综合报表', '每学期', NULL, '2026-03-02 17:41:32', '2026-03-02 17:41:32', 2, '正常');
INSERT INTO `edu_report` VALUES (15, '毕业审核预报表', '大四学分核对清查', '综合报表', '每年', NULL, '2026-03-02 17:41:32', '2026-03-02 17:41:32', 2, '正常');

-- ----------------------------
-- Table structure for edu_score
-- ----------------------------
DROP TABLE IF EXISTS `edu_score`;
CREATE TABLE `edu_score`  (
  `score_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '成绩流水主键ID',
  `student_id` bigint(0) NOT NULL COMMENT '学生ID(关联edu_student)',
  `plan_id` bigint(0) NOT NULL COMMENT '开课计划ID(关联edu_course_plan)',
  `daily_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '平时成绩',
  `homework_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '作业成绩',
  `mid_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '期中成绩',
  `final_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '期末成绩',
  `total_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '依据权重推演的总评成绩',
  `makeup_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '补考成绩记录(防挂科后顶替原始分)',
  `grade_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评级等级(如: 优秀/良好/中等/及格/不及格)',
  `gpa` decimal(3, 2) NULL DEFAULT NULL COMMENT '单科绩点(如: 4.0/3.5)',
  `is_retake` tinyint(1) NULL DEFAULT 0 COMMENT '重修标记(0否 1是)',
  `is_locked` tinyint(1) NULL DEFAULT 0 COMMENT '单条成绩防篡改锁定状态(0否 1是，随batch提交锁定)',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '在修' COMMENT '修读状态(在修/已结课)',
  PRIMARY KEY (`score_id`) USING BTREE,
  UNIQUE INDEX `uk_student_plan`(`student_id`, `plan_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生各项成绩与选课档案底表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_score
-- ----------------------------
INSERT INTO `edu_score` VALUES (1, 1, 13, 90.00, 85.00, 88.00, 92.00, 89.60, NULL, '优秀', 4.00, 0, 1, '已结课');
INSERT INTO `edu_score` VALUES (2, 2, 13, 80.00, 82.00, 75.00, 78.00, 78.40, NULL, '中等', 2.80, 0, 1, '已结课');
INSERT INTO `edu_score` VALUES (3, 3, 13, 95.00, 90.00, 92.00, 85.00, 88.40, NULL, '良好', 3.80, 0, 1, '已结课');
INSERT INTO `edu_score` VALUES (4, 4, 13, 88.00, 85.00, 80.00, 90.00, 86.80, NULL, '良好', 3.60, 0, 1, '已结课');
INSERT INTO `edu_score` VALUES (5, 5, 13, 70.00, 75.00, 60.00, 65.00, 66.50, NULL, '及格', 1.60, 0, 1, '已结课');
INSERT INTO `edu_score` VALUES (6, 1, 14, 95.00, 95.00, 90.00, 92.00, 92.50, NULL, '优秀', 4.00, 0, 1, '已结课');
INSERT INTO `edu_score` VALUES (7, 2, 14, 80.00, 85.00, 80.00, 88.00, 85.00, NULL, '良好', 3.50, 0, 1, '已结课');
INSERT INTO `edu_score` VALUES (8, 8, 6, 88.00, 90.00, 85.00, 82.00, 84.80, NULL, '良好', 3.40, 0, 1, '已结课');
INSERT INTO `edu_score` VALUES (9, 9, 6, 95.00, 92.00, 88.00, 96.00, 93.50, NULL, '优秀', 4.00, 0, 1, '已结课');
INSERT INTO `edu_score` VALUES (10, 12, 10, 90.00, 90.00, 85.00, 88.00, 88.00, NULL, '良好', 3.80, 0, 1, '已结课');
INSERT INTO `edu_score` VALUES (11, 13, 10, 85.00, 80.00, 75.00, 70.00, 74.50, NULL, '中等', 2.40, 0, 1, '已结课');
INSERT INTO `edu_score` VALUES (12, 1, 3, 92.00, 85.00, 90.00, 94.00, 91.20, NULL, '优秀', 4.00, 0, 1, '已结课');
INSERT INTO `edu_score` VALUES (13, 2, 3, 88.00, 82.00, 80.00, 85.00, 83.70, NULL, '良好', 3.30, 0, 1, '已结课');
INSERT INTO `edu_score` VALUES (14, 3, 3, 75.00, 80.00, 65.00, 70.00, 71.50, NULL, '中等', 2.20, 0, 1, '已结课');
INSERT INTO `edu_score` VALUES (19, 1, 4, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, '在修');

-- ----------------------------
-- Table structure for edu_score_audit
-- ----------------------------
DROP TABLE IF EXISTS `edu_score_audit`;
CREATE TABLE `edu_score_audit`  (
  `audit_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '审批流水主键ID',
  `batch_id` bigint(0) NOT NULL COMMENT '针对的提交批次(关联edu_score_batch)',
  `audit_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '审批结论(通过/驳回)',
  `audit_comment` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '驳回操作意见',
  `auditor_id` bigint(0) NOT NULL COMMENT '审核管理员ID',
  `audit_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '审核操作时间',
  PRIMARY KEY (`audit_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '成绩审批流水表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_score_audit
-- ----------------------------
INSERT INTO `edu_score_audit` VALUES (1, 1, '通过', '无异常', 2, '2026-01-11 09:00:00');
INSERT INTO `edu_score_audit` VALUES (2, 2, '通过', '同意发布', 2, '2026-01-12 10:00:00');
INSERT INTO `edu_score_audit` VALUES (3, 3, '通过', '数据正常', 3, '2026-01-13 11:00:00');
INSERT INTO `edu_score_audit` VALUES (4, 4, '通过', '已核对', 3, '2026-01-14 14:00:00');
INSERT INTO `edu_score_audit` VALUES (5, 6, '通过', '分布合理', 4, '2026-01-16 16:00:00');
INSERT INTO `edu_score_audit` VALUES (6, 7, '通过', '准予归档', 4, '2026-01-17 09:30:00');
INSERT INTO `edu_score_audit` VALUES (7, 8, '通过', '已核', 5, '2026-01-18 10:30:00');
INSERT INTO `edu_score_audit` VALUES (8, 10, '驳回', '不及格率偏高，需复核', 5, '2026-01-20 09:00:00');
INSERT INTO `edu_score_audit` VALUES (9, 11, '通过', '正常', 2, '2026-01-21 11:30:00');
INSERT INTO `edu_score_audit` VALUES (10, 12, '通过', '通过', 3, '2026-01-22 14:30:00');
INSERT INTO `edu_score_audit` VALUES (11, 14, '通过', '确认', 4, '2026-01-24 09:00:00');
INSERT INTO `edu_score_audit` VALUES (12, 15, '通过', '录入完整', 5, '2026-01-25 10:00:00');
INSERT INTO `edu_score_audit` VALUES (13, 1, '通过', '二次审查', 2, '2026-01-26 10:00:00');
INSERT INTO `edu_score_audit` VALUES (14, 2, '通过', '复审完成', 3, '2026-01-27 10:00:00');
INSERT INTO `edu_score_audit` VALUES (15, 3, '通过', '归档完毕', 4, '2026-01-28 10:00:00');
INSERT INTO `edu_score_audit` VALUES (16, 9, '驳回', 'biheli', 1, '2026-03-02 21:13:09');
INSERT INTO `edu_score_audit` VALUES (17, 9, '通过', NULL, 1, '2026-03-02 23:34:56');

-- ----------------------------
-- Table structure for edu_score_batch
-- ----------------------------
DROP TABLE IF EXISTS `edu_score_batch`;
CREATE TABLE `edu_score_batch`  (
  `batch_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '提交批次主键ID',
  `plan_id` bigint(0) NOT NULL COMMENT '关联教学班级(对应edu_course_plan)',
  `submit_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '教师提交时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '待审核' COMMENT '流转状态(未提交/待审核/通过/驳回)',
  PRIMARY KEY (`batch_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '成绩提交批次表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_score_batch
-- ----------------------------
INSERT INTO `edu_score_batch` VALUES (1, 3, '2026-01-10 10:00:00', '通过');
INSERT INTO `edu_score_batch` VALUES (2, 6, '2026-01-11 11:30:00', '通过');
INSERT INTO `edu_score_batch` VALUES (3, 8, '2026-01-12 14:00:00', '通过');
INSERT INTO `edu_score_batch` VALUES (4, 10, '2026-01-13 09:15:00', '通过');
INSERT INTO `edu_score_batch` VALUES (5, 12, '2026-01-14 16:45:00', '待审核');
INSERT INTO `edu_score_batch` VALUES (6, 13, '2026-01-15 10:20:00', '通过');
INSERT INTO `edu_score_batch` VALUES (7, 14, '2026-01-16 13:10:00', '通过');
INSERT INTO `edu_score_batch` VALUES (8, 15, '2026-01-17 15:50:00', '通过');
INSERT INTO `edu_score_batch` VALUES (9, 1, '2026-03-02 23:34:12', '通过');
INSERT INTO `edu_score_batch` VALUES (10, 2, '2026-01-19 11:00:00', '驳回');
INSERT INTO `edu_score_batch` VALUES (11, 4, '2026-01-20 14:20:00', '通过');
INSERT INTO `edu_score_batch` VALUES (12, 5, '2026-01-21 16:10:00', '通过');
INSERT INTO `edu_score_batch` VALUES (13, 7, '2026-01-22 09:40:00', '待审核');
INSERT INTO `edu_score_batch` VALUES (14, 9, '2026-01-23 10:50:00', '通过');
INSERT INTO `edu_score_batch` VALUES (15, 11, '2026-01-24 15:00:00', '通过');

-- ----------------------------
-- Table structure for edu_student
-- ----------------------------
DROP TABLE IF EXISTS `edu_student`;
CREATE TABLE `edu_student`  (
  `student_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '学生主键ID',
  `user_id` bigint(0) NOT NULL COMMENT '底层绑定账号ID(关联sys_user)',
  `student_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '唯一下发学号',
  `student_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学生真实姓名',
  `gender` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '学生性别(0男/1女/2未知)',
  `grade` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在年级层(如: 2021)',
  `org_id` bigint(0) NOT NULL COMMENT '修读专业挂靠机构ID(关联sys_org)',
  `major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主修专业名称',
  `class_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所在的行政班级名称',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '在读' COMMENT '当前学籍状态(在读/休学/毕业等)',
  PRIMARY KEY (`student_id`) USING BTREE,
  UNIQUE INDEX `uk_student_no`(`student_no`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '在校学生学籍全量业务档案库' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_student
-- ----------------------------
INSERT INTO `edu_student` VALUES (1, 21, '2022001', '林晨', '0', '2022', 11, '软件工程', '软工22-1班', '在读');
INSERT INTO `edu_student` VALUES (2, 20, '2022002', '林晨测试', '0', '2022', 11, '软件工程', '软工22-1班', '在读');

-- ----------------------------
-- Table structure for edu_teacher
-- ----------------------------
DROP TABLE IF EXISTS `edu_teacher`;
CREATE TABLE `edu_teacher`  (
  `teacher_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '教师主键ID',
  `user_id` bigint(0) NOT NULL COMMENT '底层绑定的账号ID(关联sys_user)',
  `teacher_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '教师真实姓名',
  `org_id` bigint(0) NOT NULL COMMENT '所属人事院系ID(关联sys_org)',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学术评级职称(教授/讲师等)',
  `gender` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '0' COMMENT '性别(0男/1女/2未知)',
  PRIMARY KEY (`teacher_id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '教职工人员基础业务档案集装表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of edu_teacher
-- ----------------------------
INSERT INTO `edu_teacher` VALUES (1, 6, '王建', 2, '教授', '0');
INSERT INTO `edu_teacher` VALUES (2, 7, '李渊', 2, '副教授', '0');
INSERT INTO `edu_teacher` VALUES (3, 8, '张诚', 2, '讲师', '0');
INSERT INTO `edu_teacher` VALUES (4, 9, '刘芳', 3, '教授', '1');
INSERT INTO `edu_teacher` VALUES (5, 10, '陈强', 3, '副教授', '0');
INSERT INTO `edu_teacher` VALUES (6, 11, '杨华', 3, '讲师', '1');
INSERT INTO `edu_teacher` VALUES (7, 12, '赵敏', 4, '副教授', '1');
INSERT INTO `edu_teacher` VALUES (8, 13, '黄磊', 4, '讲师', '0');
INSERT INTO `edu_teacher` VALUES (9, 14, '周林', 2, '助教', '0');
INSERT INTO `edu_teacher` VALUES (10, 15, '吴伟', 2, '教授', '0');
INSERT INTO `edu_teacher` VALUES (11, 16, '郑爽', 3, '助教', '1');
INSERT INTO `edu_teacher` VALUES (12, 17, '孙丽', 4, '教授', '1');
INSERT INTO `edu_teacher` VALUES (13, 18, '马腾', 2, '副教授', '0');
INSERT INTO `edu_teacher` VALUES (14, 19, '方杰', 3, '讲师', '0');
INSERT INTO `edu_teacher` VALUES (15, 20, '秦剑', 4, '副教授', '0');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '菜单主键ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单或按钮名称',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限授权标识符',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单类型(M:目录, C:菜单, F:按钮)',
  `parent_id` bigint(0) NULL DEFAULT 0 COMMENT '父菜单ID(0为根节点)',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由访问路径',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前端Vue视图组件路径',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '#' COMMENT '菜单展示图标',
  `sort_order` int(0) NULL DEFAULT 0 COMMENT '菜单列表展示顺序',
  PRIMARY KEY (`menu_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 304 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统路由菜单与操作权限定义表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (100, '教务处管理', '', 'M', 0, 'admin', 'Layout', 'Setting', 1);
INSERT INTO `sys_menu` VALUES (101, '学术组织架构', 'admin:org:list', 'C', 100, 'AcademicStructure', 'admin/AcademicStructure', 'OfficeBuilding', 1);
INSERT INTO `sys_menu` VALUES (102, '课程信息管理', 'admin:course:list', 'C', 100, 'CourseManagement', 'admin/CourseManagement', 'Reading', 2);
INSERT INTO `sys_menu` VALUES (103, '开课计划管理', 'admin:plan:list', 'C', 100, 'CoursePlanManagement', 'admin/CoursePlanManagement', 'Calendar', 3);
INSERT INTO `sys_menu` VALUES (104, '学生信息管理', 'admin:student:list', 'C', 100, 'StudentInfoManagement', 'admin/StudentInfoManagement', 'UserFilled', 4);
INSERT INTO `sys_menu` VALUES (105, '学生选课管理', 'admin:selection:list', 'C', 100, 'CourseSelectionManagement', 'admin/CourseSelectionManagement', 'Mouse', 5);
INSERT INTO `sys_menu` VALUES (106, '成绩审核', 'admin:audit:list', 'C', 100, 'GradeAuditManagement', 'admin/GradeAuditManagement', 'Stamp', 6);
INSERT INTO `sys_menu` VALUES (107, '成绩查询与统计', 'admin:stat:list', 'C', 100, 'GradeStatistics', 'admin/GradeStatistics', 'DataAnalysis', 7);
INSERT INTO `sys_menu` VALUES (108, '报表中心', 'admin:report:list', 'C', 100, 'ReportCenter', 'admin/ReportCenter', 'PieChart', 8);
INSERT INTO `sys_menu` VALUES (109, '系统权限管理', 'admin:permission:list', 'C', 100, 'PermissionManagement', 'admin/PermissionManagement', 'Key', 9);
INSERT INTO `sys_menu` VALUES (200, '教师工作台', '', 'M', 0, 'teacher', 'Layout', 'Briefcase', 2);
INSERT INTO `sys_menu` VALUES (201, '教师课程管理', 'teacher:course:list', 'C', 200, 'TeacherCourseManagement', 'teacher/TeacherCourseManagement', 'Tickets', 1);
INSERT INTO `sys_menu` VALUES (202, '成绩录入', 'teacher:grade:entry', 'C', 200, 'GradeEntry', 'teacher/GradeEntry', 'EditPen', 2);
INSERT INTO `sys_menu` VALUES (203, '成绩提交状态', 'teacher:grade:submit', 'C', 200, 'GradeSubmitStatus', 'teacher/GradeSubmitStatus', 'CircleCheck', 3);
INSERT INTO `sys_menu` VALUES (204, '教师成绩查询统计', 'teacher:grade:query', 'C', 200, 'TeacherGradeQuery', 'teacher/TeacherGradeQuery', 'Search', 4);
INSERT INTO `sys_menu` VALUES (300, '学生服务厅', '', 'M', 0, 'student', 'Layout', 'Avatar', 3);
INSERT INTO `sys_menu` VALUES (301, '学生成绩查询', 'student:grade:query', 'C', 300, 'StudentGradeQuery', 'student/StudentGradeQuery', 'DataLine', 1);
INSERT INTO `sys_menu` VALUES (302, '统计分析服务', 'student:stat:service', 'C', 300, 'StatisticService', 'student/StatisticService', 'TrendCharts', 2);
INSERT INTO `sys_menu` VALUES (303, '成绩证明申请', 'student:cert:apply', 'C', 300, 'CertificateApplication', 'student/CertificateApplication', 'Document', 3);

-- ----------------------------
-- Table structure for sys_org
-- ----------------------------
DROP TABLE IF EXISTS `sys_org`;
CREATE TABLE `sys_org`  (
  `org_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '机构主键ID',
  `org_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '机构名称',
  `org_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '机构类型(学校/学院/专业/班级)',
  `parent_id` bigint(0) NULL DEFAULT 0 COMMENT '父级机构ID(0为学校顶级)',
  `sort_order` int(0) NULL DEFAULT 0 COMMENT '组织架构树展示排序',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '当前机构启停状态(0:停用, 1:正常)',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '系统建制时间',
  PRIMARY KEY (`org_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '大学级联组织架构树表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_org
-- ----------------------------
INSERT INTO `sys_org` VALUES (1, '星海大学', '学校', 0, 1, 1, '2026-03-02 17:41:31');
INSERT INTO `sys_org` VALUES (2, '计算机科学与技术学院', '学院', 1, 1, 1, '2026-03-02 17:41:31');
INSERT INTO `sys_org` VALUES (3, '经济与管理学院', '学院', 1, 2, 1, '2026-03-02 17:41:31');
INSERT INTO `sys_org` VALUES (4, '外国语学院', '学院', 1, 3, 1, '2026-03-02 17:41:31');
INSERT INTO `sys_org` VALUES (5, '软件工程系', '专业', 2, 1, 1, '2026-03-02 17:41:31');
INSERT INTO `sys_org` VALUES (6, '人工智能系', '专业', 2, 2, 1, '2026-03-02 17:41:31');
INSERT INTO `sys_org` VALUES (7, '金融学系', '专业', 3, 1, 1, '2026-03-02 17:41:31');
INSERT INTO `sys_org` VALUES (8, '会计学系', '专业', 3, 2, 1, '2026-03-02 17:41:31');
INSERT INTO `sys_org` VALUES (9, '英语系', '专业', 4, 1, 1, '2026-03-02 17:41:31');
INSERT INTO `sys_org` VALUES (10, '翻译系', '专业', 4, 2, 1, '2026-03-02 17:41:31');
INSERT INTO `sys_org` VALUES (11, '软工22-1班', '班级', 5, 1, 1, '2026-03-02 17:41:31');
INSERT INTO `sys_org` VALUES (12, '软工22-2班', '班级', 5, 2, 1, '2026-03-02 17:41:31');
INSERT INTO `sys_org` VALUES (13, 'AI22-1班', '班级', 6, 1, 1, '2026-03-02 17:41:31');
INSERT INTO `sys_org` VALUES (14, '金融22-1班', '班级', 7, 1, 1, '2026-03-02 17:41:31');
INSERT INTO `sys_org` VALUES (15, '会计22-1班', '班级', 8, 1, 1, '2026-03-02 17:41:31');
INSERT INTO `sys_org` VALUES (16, '英语22-1班', '班级', 9, 1, 1, '2026-03-02 17:41:31');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '角色主键ID',
  `role_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色权限字符(如: admin/teacher/student)',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色展示名称',
  `role_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色详细说明',
  `role_sort` int(0) NULL DEFAULT 0 COMMENT '列表排序优先级',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '角色状态(0:停用, 1:正常)',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`role_id`) USING BTREE,
  UNIQUE INDEX `uk_role_key`(`role_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'RBAC角色定义表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 'admin', '教务处管理员', '管理全校学术架构、课程、计划及权限', 1, 1, '2026-02-24 21:27:00');
INSERT INTO `sys_role` VALUES (2, 'teacher', '专任教师', '负责排课查看、成绩录入及提交', 2, 1, '2026-02-24 21:27:00');
INSERT INTO `sys_role` VALUES (3, 'student', '在校学生', '查询成绩、申请证明及统计分析11', 3, 1, '2026-02-24 21:27:00');

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` bigint(0) NOT NULL COMMENT '关联sys_role',
  `menu_id` bigint(0) NOT NULL COMMENT '关联sys_menu',
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'RBAC角色与菜单映射关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (1, 100);
INSERT INTO `sys_role_menu` VALUES (1, 101);
INSERT INTO `sys_role_menu` VALUES (1, 102);
INSERT INTO `sys_role_menu` VALUES (1, 103);
INSERT INTO `sys_role_menu` VALUES (1, 104);
INSERT INTO `sys_role_menu` VALUES (1, 105);
INSERT INTO `sys_role_menu` VALUES (1, 106);
INSERT INTO `sys_role_menu` VALUES (1, 107);
INSERT INTO `sys_role_menu` VALUES (1, 108);
INSERT INTO `sys_role_menu` VALUES (1, 109);
INSERT INTO `sys_role_menu` VALUES (2, 200);
INSERT INTO `sys_role_menu` VALUES (2, 201);
INSERT INTO `sys_role_menu` VALUES (2, 202);
INSERT INTO `sys_role_menu` VALUES (2, 203);
INSERT INTO `sys_role_menu` VALUES (2, 204);
INSERT INTO `sys_role_menu` VALUES (3, 300);
INSERT INTO `sys_role_menu` VALUES (3, 301);
INSERT INTO `sys_role_menu` VALUES (3, 302);
INSERT INTO `sys_role_menu` VALUES (3, 303);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '用户主键ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号(唯一)',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密登录密码',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户真实姓名或昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '账号状态(0:停用, 1:正常)',
  `last_login_time` datetime(0) NULL DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 36 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统账号基础认证表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$X8hLJ1mbW4R981GEyepjNupeIWb8ngJDigbIpzKaic3xWES2feEWS', '超级管理员', NULL, 1, NULL, '2026-03-02 17:41:32', '2026-03-02 17:41:32');
INSERT INTO `sys_user` VALUES (6, 'teacher', '$2a$10$GpGFX3gf4l3SfGPtoiil/.HYGXXWTD1hVRmZT39ZeClMk4kCfF/Gm', '王建', NULL, 1, NULL, '2026-03-02 17:41:32', '2026-03-02 17:43:31');
INSERT INTO `sys_user` VALUES (21, 'student', '$2a$10$GpGFX3gf4l3SfGPtoiil/.HYGXXWTD1hVRmZT39ZeClMk4kCfF/Gm', '林晨', NULL, 1, NULL, '2026-03-02 17:41:32', '2026-03-02 17:43:36');
INSERT INTO `sys_user` VALUES (36, '测试', '$2a$10$cKKzEF39aVKhvTUEUnK37u.w.I5psKUUkKKJLmP1gv6qRIlN.UvWC', '1', NULL, 1, NULL, '2026-03-02 23:39:00', '2026-03-02 23:39:00');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint(0) NOT NULL COMMENT '关联sys_user(PK/FK)',
  `role_id` bigint(0) NOT NULL COMMENT '关联sys_role(PK/FK)',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'RBAC用户与角色映射关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1);
INSERT INTO `sys_user_role` VALUES (2, 1);
INSERT INTO `sys_user_role` VALUES (3, 1);
INSERT INTO `sys_user_role` VALUES (4, 1);
INSERT INTO `sys_user_role` VALUES (5, 1);
INSERT INTO `sys_user_role` VALUES (6, 2);
INSERT INTO `sys_user_role` VALUES (7, 2);
INSERT INTO `sys_user_role` VALUES (8, 2);
INSERT INTO `sys_user_role` VALUES (9, 2);
INSERT INTO `sys_user_role` VALUES (10, 2);
INSERT INTO `sys_user_role` VALUES (11, 2);
INSERT INTO `sys_user_role` VALUES (12, 2);
INSERT INTO `sys_user_role` VALUES (13, 2);
INSERT INTO `sys_user_role` VALUES (14, 2);
INSERT INTO `sys_user_role` VALUES (15, 2);
INSERT INTO `sys_user_role` VALUES (16, 2);
INSERT INTO `sys_user_role` VALUES (17, 2);
INSERT INTO `sys_user_role` VALUES (18, 2);
INSERT INTO `sys_user_role` VALUES (19, 2);
INSERT INTO `sys_user_role` VALUES (20, 2);
INSERT INTO `sys_user_role` VALUES (21, 3);
INSERT INTO `sys_user_role` VALUES (22, 3);
INSERT INTO `sys_user_role` VALUES (23, 3);
INSERT INTO `sys_user_role` VALUES (24, 3);
INSERT INTO `sys_user_role` VALUES (25, 3);
INSERT INTO `sys_user_role` VALUES (26, 3);
INSERT INTO `sys_user_role` VALUES (27, 3);
INSERT INTO `sys_user_role` VALUES (28, 3);
INSERT INTO `sys_user_role` VALUES (29, 3);
INSERT INTO `sys_user_role` VALUES (30, 3);
INSERT INTO `sys_user_role` VALUES (31, 3);
INSERT INTO `sys_user_role` VALUES (32, 3);
INSERT INTO `sys_user_role` VALUES (33, 3);
INSERT INTO `sys_user_role` VALUES (34, 3);
INSERT INTO `sys_user_role` VALUES (35, 3);
INSERT INTO `sys_user_role` VALUES (36, 2);

SET FOREIGN_KEY_CHECKS = 1;
