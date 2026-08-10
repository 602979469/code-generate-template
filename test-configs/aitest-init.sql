-- 测试数据库初始化（aitest）：建库 + 测试表 + 样例数据
-- 用途：code-generate-template 生成器功能测试

CREATE DATABASE IF NOT EXISTS `aitest` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `aitest`;

-- ------------------------------------------------------------------
-- sys_dept：标准 CRUD + 默认类型映射全覆盖
-- bigint / int / tinyint / varchar / char / text / datetime / date / decimal / double / bit
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
  `id`                 bigint        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `create_time`        datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time`        datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `dept_name`          varchar(50)   NOT NULL COMMENT '部门名称',
  `dept_code`          char(20)      DEFAULT NULL COMMENT '部门编码',
  `description`        text          DEFAULT NULL COMMENT '描述',
  `sort_no`            int           NOT NULL DEFAULT 0 COMMENT '排序号',
  `level`              tinyint       NOT NULL DEFAULT 1 COMMENT '层级',
  `parent_id`          bigint        DEFAULT NULL COMMENT '上级部门ID',
  `budget`             decimal(12,2) DEFAULT NULL COMMENT '预算',
  `score`              double        DEFAULT NULL COMMENT '评分',
  `enabled`            bit(1)        DEFAULT b'1' COMMENT '是否启用',
  `establish_date`     date          DEFAULT NULL COMMENT '成立日期',
  `last_meeting_time`  datetime      DEFAULT NULL COMMENT '最近会议时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- ------------------------------------------------------------------
-- sys_job_log：内部表（generateController: false）+ int 枚举 + varchar 枚举
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log` (
  `id`          bigint        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `create_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `job_name`    varchar(64)   NOT NULL COMMENT '任务名称',
  `user_type`   int           NOT NULL DEFAULT 0 COMMENT '用户类型(0系统用户 1普通用户)',
  `status`      varchar(20)   NOT NULL DEFAULT 'SUCCESS' COMMENT '执行状态(SUCCESS/FAILED)',
  `cost_time`   bigint        NOT NULL DEFAULT 0 COMMENT '耗时(ms)',
  `error_msg`   text          DEFAULT NULL COMMENT '错误信息',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务日志表';

-- ------------------------------------------------------------------
-- member：表级逻辑删除（表级覆盖全局：delete_value=2 用于验证覆盖生效）
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `member`;
CREATE TABLE `member` (
  `id`          bigint        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `create_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag`    tinyint       NOT NULL DEFAULT 0 COMMENT '删除标志(0正常 2已删除)',
  `member_name` varchar(50)   NOT NULL COMMENT '会员名称',
  `phone`       varchar(20)   DEFAULT NULL COMMENT '手机号',
  `level`       int           NOT NULL DEFAULT 1 COMMENT '会员等级',
  `balance`     decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '余额',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员表';

-- ------------------------------------------------------------------
-- 样例数据
-- ------------------------------------------------------------------
INSERT INTO `sys_dept` (`dept_name`, `dept_code`, `description`, `sort_no`, `level`, `parent_id`, `budget`, `score`, `enabled`, `establish_date`, `last_meeting_time`) VALUES
('技术部', 'TECH', '研发部门', 1, 1, NULL, 100000.00, 98.5, b'1', '2020-01-01', '2026-01-15 10:00:00'),
('产品部', 'PROD', '产品部门', 2, 1, NULL, 80000.00, 95.0, b'1', '2021-03-15', '2026-02-20 14:30:00'),
('运维部', 'OPS', '运维部门', 3, 1, NULL, 50000.00, 92.0, b'0', '2022-06-01', NULL);

INSERT INTO `sys_job_log` (`job_name`, `user_type`, `status`, `cost_time`, `error_msg`) VALUES
('定时清理任务', 0, 'SUCCESS', 1200, NULL),
('报表生成任务', 1, 'FAILED', 5800, '生成超时');

INSERT INTO `member` (`member_name`, `phone`, `level`, `balance`) VALUES
('张三', '13800000001', 2, 100.50),
('李四', '13800000002', 1, 0.00);

-- ------------------------------------------------------------------
-- json_test：json 列测试（列级配置负向/正向用）
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `json_test`;
CREATE TABLE `json_test` (
  `id`          bigint        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `create_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `payload`     json          DEFAULT NULL COMMENT 'JSON数据',
  `remark`      varchar(100)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='JSON测试表';

-- ------------------------------------------------------------------
-- dict_item：枚举冲突场景测试（枚举已存在 → 跳过整表）
-- ------------------------------------------------------------------
DROP TABLE IF EXISTS `dict_item`;
CREATE TABLE `dict_item` (
  `id`          bigint        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `create_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `dict_type`   int           NOT NULL DEFAULT 0 COMMENT '字典类型(0系统 1业务)',
  `dict_name`   varchar(50)   NOT NULL COMMENT '字典名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典项表';
