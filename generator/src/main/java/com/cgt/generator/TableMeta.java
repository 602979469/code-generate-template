package com.cgt.generator;

import java.util.ArrayList;
import java.util.List;

/**
 * 表级生成所需的完整元信息。
 */
public final class TableMeta {

    public String tableName;
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
}
