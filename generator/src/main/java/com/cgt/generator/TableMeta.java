package com.cgt.generator;

import java.util.ArrayList;
import java.util.List;

/**
 * 表级生成所需的完整元信息。
 */
public final class TableMeta {

    public String tableName;
    public String tableComment;
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
}
