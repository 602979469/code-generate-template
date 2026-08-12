-- ------------------------------------------------------------------
-- 示例表 DDL（三张表，覆盖 code-generate-template 全部配置项演示）
-- 由 ./gen.sh 无参运行时复制为当前目录 example.sql，建表后可跑 ./gen.sh ./generate.yaml
-- 全部使用 CREATE TABLE IF NOT EXISTS + INSERT IGNORE，重复执行幂等
-- ------------------------------------------------------------------

-- ------------------------------------------------------------------
-- 1) example_inner：内部表（generateController: false）
--    演示: 不生成 Controller / ParamChecker / DTO / Assembler（web 层 7 个文件）
--    类型: 仅默认映射（varchar->String / bigint->Long / text->String）
-- ------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `example_inner` (
  `id`          bigint        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `create_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `job_name`    varchar(64)   NOT NULL COMMENT '任务名称',
  `status`      varchar(20)   NOT NULL DEFAULT 'SUCCESS' COMMENT '执行状态(SUCCESS/FAILED)',
  `error_msg`   text          DEFAULT NULL COMMENT '错误信息',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内部示例表';

INSERT IGNORE INTO `example_inner` (`id`, `job_name`, `status`, `error_msg`)
VALUES
  (1, '每日同步', 'SUCCESS', NULL),
  (2, '夜间备份', 'FAILED', '磁盘空间不足');

-- ------------------------------------------------------------------
-- 2) example_logic：逻辑删除表
--    演示: logicDelete（deleteById 变 UPDATE、查询自动过滤），仅默认类型映射，不配置 columns
--    类型: varchar->String / int->Integer / decimal->BigDecimal
-- ------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `example_logic` (
  `id`          bigint        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `create_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag`    tinyint       NOT NULL DEFAULT 0 COMMENT '删除标志(0正常 1已删除)',
  `user_name`   varchar(30)   NOT NULL COMMENT '用户名',
  `age`         int           DEFAULT NULL COMMENT '年龄',
  `amount`      decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '金额',
  `remark`      varchar(200)  DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='逻辑删除示例表';

INSERT IGNORE INTO `example_logic` (`id`, `user_name`, `age`, `amount`, `remark`, `del_flag`)
VALUES
  (1, 'alice', 18, 99.50, '正常行', 0),
  (2, 'bob', NULL, 0.00, '正常行', 0),
  (3, 'deleted-user', 30, 200.00, '已删除行', 1);

-- ------------------------------------------------------------------
-- 3) example：全功能表（非逻辑删除）
--    演示: 枚举（int / varchar）、json / jsonArray / jsonObject、强制类型转换、force_create
--    类型: bigint->Long、int/tinyint->Integer、decimal->BigDecimal、varchar->String、json->String
-- ------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `example` (
  `id`          bigint        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `create_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `user_name`   varchar(30)   NOT NULL COMMENT '用户名',
  `password`    varchar(100)  NOT NULL COMMENT '密码',
  `nick_name`   varchar(50)   DEFAULT NULL COMMENT '昵称',
  `age`         int           DEFAULT NULL COMMENT '年龄',
  `level`       tinyint       NOT NULL DEFAULT 0 COMMENT '等级',
  `login_count` bigint        NOT NULL DEFAULT 0 COMMENT '登录次数(强制转 Integer)',
  `balance`     decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '余额',
  `user_type`   int           NOT NULL DEFAULT 0 COMMENT '用户类型(0系统用户 1普通用户)',
  `status`      varchar(20)   NOT NULL DEFAULT 'ENABLED' COMMENT '状态(ENABLED/DISABLED)',
  `profile`     json          DEFAULT NULL COMMENT '扩展信息(json 对象)',
  `tags`        json          DEFAULT NULL COMMENT '标签(json 数组)',
  `extra`       json          DEFAULT NULL COMMENT '扩展原始JSON(json 原样)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全功能示例表';

INSERT IGNORE INTO `example`
  (`id`, `user_name`, `password`, `nick_name`, `age`, `level`, `login_count`, `balance`,
   `user_type`, `status`, `profile`, `tags`, `extra`)
VALUES
  (1, 'alice', 'secret-1', '爱丽丝', 18, 2, 5, 99.50, 0, 'ENABLED',
   '{"nickName":"爱丽丝","email":"alice@example.com"}',
   '[{"name":"标签A"},{"name":"标签B"}]',
   '{"source":"seed"}'),
  (2, 'bob', 'secret-2', NULL, NULL, 1, 0, 0.00, 1, 'DISABLED',
   NULL, NULL, NULL),
  (3, 'carol', 'secret-3', '卡罗尔', 25, 1, 8, 66.66, 1, 'ENABLED',
   NULL,
   '[{"name":"标签C"},{"name":"标签D"}]',
   '{"level":"vip"}');
