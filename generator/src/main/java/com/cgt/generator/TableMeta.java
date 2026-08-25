package com.cgt.generator;

import java.util.ArrayList;
import java.util.List;

/**
 * 表级生成所需的完整元信息。
 */
public final class TableMeta {

    public String tableName;
    /** 所属业务模块（表级 module 配置，可空；决定业务层包路径中的模块段）。 */
    public String module;
    /** 实体中文名：由配置 model_comment 提供(如 用户)，生成代码的所有 javadoc/日志注释都用它拼接。 */
    public String entityName;
    public String className;
    public String classNameLower;

    /** 业务字段（不含 id/create_time/update_time 及保留审计列）。 */
    public final List<ColumnMeta> columns = new ArrayList<>();
    public final List<ColumnMeta> queryColumns = new ArrayList<>();
    public final List<ColumnMeta> requiredColumns = new ArrayList<>();

    public String selectColumns;
    public String insertColumns;
    public String insertValues;
    public String updateSet;

    public boolean hasLocalDateTime;
    public boolean hasLocalDate;
    public boolean hasBigDecimal;
    /** 是否存在必填字符串列（决定 DTO 是否导入 @NotBlank/@Size）。 */
    public boolean hasRequiredString;
    /** 是否存在必填非字符串列（决定 DTO 是否导入 @NotNull）。 */
    public boolean hasRequiredNonString;
    /** 是否存在字符串列（决定 DTO 是否导入 @Size）。 */
    public boolean hasString;

    /** 逻辑删除是否启用（配置生效且列存在）。 */
    public boolean logicDeleteEnabled;
    /** 逻辑删除配置开启但列不存在时的提示（非空时表级框展示，生成退化为物理删除）。 */
    public String logicDeleteWarn;
    /** 逻辑删除列名。 */
    public String logicDeleteColumn;
    /** 未删除值（SQL 字面量，字符串列已加引号）。 */
    public String logicDeleteNormal;
    /** 已删除值（SQL 字面量）。 */
    public String logicDeleteDelete;

    /** 主键列名（按 PRIMARY KEY 元数据识别，不假设为 id）。 */
    public String pkColumnName;
    /** 主键属性名（user_id -> userId）。 */
    public String pkPropertyName;
    /** 主键 Java 类型（bigint->Long、varchar->String...）。 */
    public String pkJavaType;
    /** 主键是否自增。 */
    public boolean pkAuto;
    /** 全部主键列（单主键时 1 个；复合主键时多个，关联表模式使用）。 */
    public final List<ColumnMeta> pkColumns = new ArrayList<>();
    /** 是否复合主键：关联表模式，只生成 DO/Mapper/Mapper.xml。 */
    public boolean compositePk;
    /** create_time 是否由数据库自动维护（DEFAULT CURRENT_TIMESTAMP）。 */
    public boolean createTimeAuto;
    /** update_time 是否由数据库自动维护（DEFAULT CURRENT_TIMESTAMP + ON UPDATE）。 */
    public boolean updateTimeAuto;
}
