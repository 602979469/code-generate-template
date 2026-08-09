package com.cgt.generator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 读取 information_schema 表结构，生成类名/字段/查询条件/SQL 片段。
 */
public final class DbMetaReader {

    /** BaseDO 强约束字段，不生成到 DO。 */
    private static final Set<String> BASE_COLUMNS = Set.of("id", "create_time", "update_time");

    /** 保留审计字段，后续 BizDO 启用，当前不生成。 */
    private static final Set<String> RESERVED = Set.of("create_by", "update_by", "del_flag");

    private DbMetaReader() {
    }

    public static TableMeta read(GeneratorConfig cfg, String tableName) {
        TableMeta table = new TableMeta();
        table.tableName = tableName;
        String bare = stripPrefix(tableName, cfg.tablePrefix);
        table.className = toUpperCamel(bare);
        table.classNameLower = toLowerCamel(bare);

        try (Connection conn = DriverManager.getConnection(cfg.jdbcUrl, cfg.jdbcUsername, cfg.jdbcPassword)) {
            String schema = conn.getCatalog();
            String sql = """
                    SELECT column_name, data_type, character_maximum_length, column_comment,
                           is_nullable, column_key, column_default, extra
                    FROM information_schema.columns
                    WHERE table_schema = ? AND table_name = ?
                    ORDER BY ordinal_position
                    """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, schema);
                ps.setString(2, tableName);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ColumnMeta column = parseColumn(rs);
                        if (BASE_COLUMNS.contains(column.columnName) || RESERVED.contains(column.columnName)) {
                            continue;
                        }
                        table.columns.add(column);
                        if (column.required) {
                            table.requiredColumns.add(column);
                        }
                        table.hasLocalDateTime |= "LocalDateTime".equals(column.javaType);
                        table.hasLocalDate |= "LocalDate".equals(column.javaType);
                        table.hasBigDecimal |= "BigDecimal".equals(column.javaType);
                    }
                }
            }
            // 查询条件 = id + 全部业务字段 + 创建/更新时间，程序员按需删减
            buildQueryColumns(table);
            table.tableComment = readTableComment(conn, schema, tableName);
        } catch (SQLException e) {
            throw new IllegalStateException("读取表结构失败: " + tableName, e);
        }

        buildSqlFragments(table);
        return table;
    }

    private static ColumnMeta parseColumn(ResultSet rs) throws SQLException {
        ColumnMeta c = new ColumnMeta();
        c.columnName = rs.getString("column_name");
        c.propertyName = toLowerCamel(c.columnName);
        c.javaType = mapJavaType(rs.getString("data_type"));
        c.string = "String".equals(c.javaType);
        c.length = rs.getInt("character_maximum_length");
        c.comment = rs.getString("column_comment");
        String key = rs.getString("column_key");
        c.pk = "PRI".equals(key);
        c.auto = rs.getString("extra") != null && rs.getString("extra").contains("auto_increment");
        boolean nullable = "YES".equals(rs.getString("is_nullable"));
        String defaultValue = rs.getString("column_default");
        c.required = !c.pk && !c.auto && !nullable && defaultValue == null;
        c.queryType = queryType(c);
        if (c.comment == null || c.comment.isBlank()) {
            c.comment = c.columnName;
        }
        return c;
    }

    private static String queryType(ColumnMeta c) {
        if (c.string) {
            return "status".equals(c.columnName) ? "EQ" : "LIKE";
        }
        return "EQ";
    }

    private static void buildQueryColumns(TableMeta table) {
        addQueryColumn(table, "id", "id", "Long", "主键ID");
        table.queryColumns.addAll(table.columns);
        addQueryColumn(table, "create_time", "createTime", "LocalDateTime", "创建时间");
        addQueryColumn(table, "update_time", "updateTime", "LocalDateTime", "更新时间");
    }

    private static void addQueryColumn(TableMeta table, String columnName, String propertyName,
                                       String javaType, String comment) {
        ColumnMeta c = new ColumnMeta();
        c.columnName = columnName;
        c.propertyName = propertyName;
        c.javaType = javaType;
        c.comment = comment;
        c.string = "String".equals(javaType);
        c.queryType = queryType(c);
        table.queryColumns.add(c);
    }

    private static void buildSqlFragments(TableMeta table) {
        List<ColumnMeta> cols = table.columns;
        table.selectColumns = "id, " + cols.stream().map(c -> c.columnName).collect(Collectors.joining(", "))
                + ", create_time, update_time";

        List<ColumnMeta> insertCols = cols.stream().filter(c -> !c.auto).collect(Collectors.toList());
        table.insertColumns = insertCols.stream().map(c -> c.columnName).collect(Collectors.joining(", "))
                + ", create_time, update_time";
        table.insertValues = insertCols.stream().map(c -> "#{" + c.propertyName + "}").collect(Collectors.joining(", "))
                + ", #{createTime}, #{updateTime}";

        List<ColumnMeta> updateCols = cols.stream().filter(c -> !c.auto && !c.pk).collect(Collectors.toList());
        table.updateSet = updateCols.stream().map(c -> c.columnName + " = #{" + c.propertyName + "}")
                .collect(Collectors.joining(",\n            "))
                + ",\n            update_time = #{updateTime}";
    }

    private static String readTableComment(Connection conn, String schema, String tableName) throws SQLException {
        String sql = "SELECT table_comment FROM information_schema.tables WHERE table_schema = ? AND table_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, schema);
            ps.setString(2, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String comment = rs.getString(1);
                    return comment == null || comment.isBlank() ? tableName : comment;
                }
            }
        }
        return tableName;
    }

    private static String mapJavaType(String dataType) {
        return switch (dataType) {
            case "bigint" -> "Long";
            case "int", "integer", "smallint", "mediumint", "tinyint" -> "Integer";
            case "varchar", "char", "text", "longtext", "mediumtext", "tinytext" -> "String";
            case "datetime", "timestamp" -> "LocalDateTime";
            case "date" -> "LocalDate";
            case "decimal", "numeric" -> "BigDecimal";
            case "double", "float" -> "Double";
            case "bit", "boolean", "bool" -> "Boolean";
            default -> "String";
        };
    }

    public static String stripPrefix(String tableName, String prefix) {
        if (prefix != null && !prefix.isBlank() && tableName.startsWith(prefix)) {
            return tableName.substring(prefix.length());
        }
        return tableName;
    }

    public static String toUpperCamel(String name) {
        StringBuilder sb = new StringBuilder();
        boolean upper = true;
        for (char ch : name.toCharArray()) {
            if (ch == '_') {
                upper = true;
            } else if (upper) {
                sb.append(Character.toUpperCase(ch));
                upper = false;
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public static String toLowerCamel(String name) {
        String upper = toUpperCamel(name);
        return upper.isEmpty() ? upper : Character.toLowerCase(upper.charAt(0)) + upper.substring(1);
    }
}
