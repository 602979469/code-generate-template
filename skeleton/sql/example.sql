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
