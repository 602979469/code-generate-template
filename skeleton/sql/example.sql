-- ------------------------------------------------------------------
-- 表: example（示例表：覆盖常用数据类型演示，含逻辑删除）
-- 由 generateExample: true 在生成时执行（CREATE TABLE IF NOT EXISTS）
-- ------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `example` (
  `id`          bigint        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `create_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag`    tinyint       NOT NULL DEFAULT 0 COMMENT '删除标志(0正常 1已删除)',

  `user_name`   varchar(30)   NOT NULL COMMENT '用户名',
  `password`    varchar(100)  NOT NULL COMMENT '密码',
  `nick_name`   varchar(50)   DEFAULT NULL COMMENT '昵称',
  `age`         int           DEFAULT NULL COMMENT '年龄',
  `level`       tinyint       NOT NULL DEFAULT 0 COMMENT '等级',
  `login_count` bigint        NOT NULL DEFAULT 0 COMMENT '登录次数',
  `balance`     decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '余额',
  `user_type`   int           NOT NULL DEFAULT 0 COMMENT '用户类型(0系统用户 1普通用户)',
  `status`      varchar(20)   NOT NULL DEFAULT 'ENABLED' COMMENT '状态(ENABLED/DISABLED)',
  `profile`     json          DEFAULT NULL COMMENT '扩展信息(json 对象)',
  `tags`        json          DEFAULT NULL COMMENT '标签(json 数组)',
  `extra`       json          DEFAULT NULL COMMENT '扩展原始JSON(默认映射String)',

  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='示例表';

-- ------------------------------------------------------------------
-- 样例数据（幂等：显式主键 + INSERT IGNORE，重复执行不会重复插入）
-- 覆盖场景：
--   id=1 正常行：int 枚举(0 SYSTEM_USER) + varchar 枚举(ENABLED) + jsonObject/jsonArray/json 全有内容
--   id=2 正常行：int 枚举(1 NORMAL_USER) + varchar 枚举(DISABLED) + 可空字段全空(NULL)
--   id=3 逻辑删除行：del_flag=1（查询默认过滤，演示逻辑删除）
--   id=4 正常行：int 枚举(1) + jsonArray 有内容 + jsonObject 为空
-- ------------------------------------------------------------------
INSERT IGNORE INTO `example`
  (`id`, `user_name`, `password`, `nick_name`, `age`, `level`, `login_count`, `balance`,
   `user_type`, `status`, `profile`, `tags`, `extra`, `del_flag`)
VALUES
  (1, 'alice', 'secret-1', '爱丽丝', 18, 2, 5, 99.50, 0, 'ENABLED',
   '{"nickName":"爱丽丝","email":"alice@example.com"}',
   '[{"name":"标签A"},{"name":"标签B"}]',
   '{"source":"seed"}', 0),
  (2, 'bob', 'secret-2', NULL, NULL, 1, 0, 0.00, 1, 'DISABLED',
   NULL, NULL, NULL, 0),
  (3, 'deleted-user', 'secret-3', '已删除用户', 30, 3, 10, 200.00, 0, 'ENABLED',
   '{"nickName":"已删除用户","email":"del@example.com"}',
   '[{"name":"旧标签"}]',
   NULL, 1),
  (4, 'carol', 'secret-4', '卡罗尔', 25, 1, 8, 66.66, 1, 'ENABLED',
   NULL,
   '[{"name":"标签C"},{"name":"标签D"}]',
   '{"level":"vip"}', 0);
