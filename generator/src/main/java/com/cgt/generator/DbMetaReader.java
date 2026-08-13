package com.cgt.generator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 读取 information_schema 表结构，合并列级配置，生成类名/字段/查询条件/SQL 片段与转换表达式。
 */
public final class DbMetaReader {

    /** 强约束审计字段：必须存在且不生成到 DO（主键不再假设叫 id，按 PRIMARY KEY 元数据识别）。 */
    private static final Set<String> BASE_COLUMNS = Set.of("create_time", "update_time");

    /** 保留审计字段，后续 BizDO 启用，当前不生成（逻辑删除列除外，由 SQL 层管理）。 */
    private static final Set<String> RESERVED = Set.of("create_by", "update_by", "del_flag");

    private DbMetaReader() {
    }

    public static TableMeta read(GeneratorConfig cfg, GeneratorConfig.TableConfig table) {
        TableMeta meta = new TableMeta();
        meta.tableName = table.dbTableName;
        meta.className = table.modelName;
        meta.classNameLower = toLowerCamel(table.modelName);
        // model_comment 缺省时取数据库表注释，仍为空则回退表名
        meta.entityName = table.modelComment;

        // 原始列名 -> 数据类型（含 id/审计列，用于逻辑删除列存在性与 SQL 字面量引号判断）
        Map<String, String> rawColumns = new LinkedHashMap<>();
        // 列默认值 / 额外属性（用于强约束校验 create_time/update_time 是否由数据库自动维护）
        Map<String, String> defaultMap = new LinkedHashMap<>();
        Map<String, String> extraMap = new LinkedHashMap<>();

        try (Connection conn = DriverManager.getConnection(cfg.jdbcUrl, cfg.jdbcUsername, cfg.jdbcPassword)) {
            String schema = conn.getCatalog();
            // 表存在性前置校验：避免先落半成品文件再报错
            try (PreparedStatement existsPs = conn.prepareStatement(
                    "SELECT COUNT(*), MAX(table_comment) FROM information_schema.tables "
                            + "WHERE table_schema = ? AND table_name = ?")) {
                existsPs.setString(1, schema);
                existsPs.setString(2, table.dbTableName);
                try (ResultSet existsRs = existsPs.executeQuery()) {
                    existsRs.next();
                    if (existsRs.getInt(1) == 0) {
                        throw new IllegalStateException("表不存在: " + table.dbTableName
                                + "（请先建表或在配置中修正 db_table_name）");
                    }
                    if (meta.entityName == null || meta.entityName.isBlank()) {
                        meta.entityName = existsRs.getString(2);
                    }
                }
            }
            if (meta.entityName == null || meta.entityName.isBlank()) {
                meta.entityName = table.dbTableName;
            }
            String sql = """
                    SELECT column_name, data_type, character_maximum_length, column_comment,
                           is_nullable, column_key, column_default, extra
                    FROM information_schema.columns
                    WHERE table_schema = ? AND table_name = ?
                    ORDER BY ordinal_position
                    """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, schema);
                ps.setString(2, table.dbTableName);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ColumnMeta column = parseColumn(rs);
                        rawColumns.put(column.columnName, rs.getString("data_type"));
                        defaultMap.put(column.columnName, rs.getString("column_default"));
                        extraMap.put(column.columnName, rs.getString("extra"));
                        if (column.pk) {
                            if (meta.pkColumnName != null) {
                                throw new IllegalStateException("表 " + table.dbTableName
                                        + " 为复合主键，暂不支持（请使用单列主键）");
                            }
                            meta.pkColumnName = column.columnName;
                            meta.pkPropertyName = column.propertyName;
                            meta.pkJavaType = column.javaType;
                            meta.pkAuto = column.auto;
                        }
                        if (column.pk || BASE_COLUMNS.contains(column.columnName) || RESERVED.contains(column.columnName)) {
                            continue;
                        }
                        meta.columns.add(column);
                        meta.hasLocalDateTime |= "LocalDateTime".equals(column.javaType);
                        meta.hasLocalDate |= "LocalDate".equals(column.javaType);
                        meta.hasBigDecimal |= "BigDecimal".equals(column.javaType);
                    }
                }
            }
            if (meta.pkColumnName == null) {
                throw new IllegalStateException("表 " + table.dbTableName + " 缺少主键（请设置单列 PRIMARY KEY）");
            }
            if (!rawColumns.containsKey("create_time") || !rawColumns.containsKey("update_time")) {
                throw new IllegalStateException("表 " + table.dbTableName
                        + " 缺少强约束字段 create_time / update_time（必须存在且按此命名）");
            }
            requireAutoTimestamp(table.dbTableName, "create_time",
                    defaultMap.get("create_time"), extraMap.get("create_time"), false);
            requireAutoTimestamp(table.dbTableName, "update_time",
                    defaultMap.get("update_time"), extraMap.get("update_time"), true);
            // 查询条件 = id + 全部业务字段 + 创建/更新时间，程序员按需删减
            buildQueryColumns(meta);
        } catch (SQLException e) {
            throw new IllegalStateException("读取表结构失败: " + table.dbTableName, e);
        }

        applyColumnConfigs(meta, table);
        resolveLogicDelete(meta, cfg, table, rawColumns);
        buildSqlFragments(meta);
        return meta;
    }

    /** 读取表的真实建表语句（SHOW CREATE TABLE），用于按 tables 配置追加到项目 init.sql。 */
    public static String readCreateTable(GeneratorConfig cfg, GeneratorConfig.TableConfig table) {
        try (Connection conn = DriverManager.getConnection(cfg.jdbcUrl, cfg.jdbcUsername, cfg.jdbcPassword)) {
            String schema = conn.getCatalog();
            String sql = "SHOW CREATE TABLE `" + schema + "`.`" + table.dbTableName + "`";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                if (rs.next()) {
                    return rs.getString("Create Table");
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("读取建表语句失败: " + table.dbTableName, e);
        }
        return null;
    }

    private static ColumnMeta parseColumn(ResultSet rs) throws SQLException {
        ColumnMeta c = new ColumnMeta();
        c.columnName = rs.getString("column_name");
        c.propertyName = toLowerCamel(c.columnName);
        c.dbType = rs.getString("data_type");
        c.javaType = mapJavaType(c.dbType);
        c.string = "String".equals(c.javaType);
        // blob/text 等类型 CHARACTER_MAXIMUM_LENGTH 为 4294967295，超出 int 范围；
        // 超长字段不生成 @Size 校验（length 置 0），仅 varchar/char 等有限长字段使用。
        long rawLength = rs.getLong("character_maximum_length");
        c.length = rawLength > Integer.MAX_VALUE ? 0 : (int) rawLength;
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

    /**
     * 强约束：create_time/update_time 必须由数据库自动维护（生成器 INSERT/UPDATE 不写这两个字段）。
     * create_time 需 DEFAULT CURRENT_TIMESTAMP；update_time 还需 ON UPDATE CURRENT_TIMESTAMP。
     */
    private static void requireAutoTimestamp(String tableName, String column,
                                             String defaultValue, String extra, boolean requireOnUpdate) {
        boolean hasDefault = defaultValue != null && defaultValue.toUpperCase().contains("CURRENT_TIMESTAMP");
        boolean hasOnUpdate = extra != null && extra.toLowerCase().contains("on update");
        if (!hasDefault || (requireOnUpdate && !hasOnUpdate)) {
            throw new IllegalStateException("表 " + tableName + " 的 " + column
                    + " 必须由数据库自动维护（生成器不写该字段）：" + column + " DEFAULT CURRENT_TIMESTAMP"
                    + (requireOnUpdate ? " ON UPDATE CURRENT_TIMESTAMP" : "")
                    + "，当前 default=" + (defaultValue == null ? "无" : defaultValue)
                    + "，extra=" + (extra == null ? "无" : extra));
        }
    }

    private static String queryType(ColumnMeta c) {
        // 当前统一等值查询；LIKE 属于业务需求，无法从建表语句推导，后续按需求/配置扩展
        return "EQ";
    }

    /**
     * 合并列级配置：type 显式声明转换逻辑；未配置的列按默认映射（modelType = javaType，直接赋值）。
     */
    private static void applyColumnConfigs(TableMeta meta, GeneratorConfig.TableConfig table) {
        Set<String> realColumns = meta.columns.stream().map(c -> c.columnName).collect(Collectors.toSet());
        for (String key : table.columns.keySet()) {
            if (!realColumns.contains(key)) {
                throw new IllegalStateException("列 " + key + " 不存在于表 " + table.dbTableName);
            }
        }

        meta.hasString = false;
        meta.hasRequiredString = false;
        meta.hasRequiredNonString = false;

        for (ColumnMeta c : meta.columns) {
            GeneratorConfig.ColumnConfig cc = table.columns.get(c.columnName);
            c.modelType = c.javaType;
            c.modelString = "String".equals(c.modelType);
            if (cc != null) {
                applyColumnConfig(meta, c, cc);
            }
            if (c.toModelExpr == null) {
                c.toModelExpr = "{do}.get" + cap(c.propertyName) + "()";
            }
            if (c.toDoExpr == null) {
                c.toDoExpr = "{model}.get" + cap(c.propertyName) + "()";
            }
            meta.hasString |= c.modelString && c.length > 0;
            if (c.required) {
                if (c.modelString) {
                    meta.hasRequiredString = true;
                } else {
                    meta.hasRequiredNonString = true;
                }
            }
        }
        // 非自增主键需由前端传入（CreateRequest 必填），计入 DTO 校验注解导入
        if (!meta.pkAuto) {
            if ("String".equals(meta.pkJavaType)) {
                meta.hasRequiredString = true;
            } else {
                meta.hasRequiredNonString = true;
            }
        }
    }

    private static void applyColumnConfig(TableMeta meta, ColumnMeta c, GeneratorConfig.ColumnConfig cc) {
        String type = normalizeJavaType(cc.type);
        String getter = "get" + cap(c.propertyName) + "()";
        switch (type) {
            case "enum" -> applyEnumConfig(meta, c, cc, getter);
            case "json" -> {
                c.conversion = "JSON";
                c.modelType = "String";
            }
            case "jsonArray" -> applyJsonArrayConfig(meta, c, cc, getter);
            case "jsonObject" -> applyJsonObjectConfig(meta, c, cc, getter);
            default -> applyCoerceConfig(c, type, getter);
        }
        c.modelString = "String".equals(c.modelType);
    }

    private static void applyEnumConfig(TableMeta meta, ColumnMeta c, GeneratorConfig.ColumnConfig cc, String getter) {
        if (cc.enumConfig == null || cc.enumConfig.values.isEmpty()) {
            throw new IllegalStateException("列 " + c.columnName + " type: enum 时必须配置 enum 块");
        }
        String codeType = cc.enumConfig.codeType;
        if (codeType == null) {
            codeType = switch (c.javaType) {
                case "Integer" -> "Integer";
                case "Long" -> "Long";
                default -> "String";
            };
        }
        for (GeneratorConfig.EnumValue v : cc.enumConfig.values) {
            if ("Integer".equals(codeType)) {
                try {
                    Integer.parseInt(v.code);
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("列 " + c.columnName + " 枚举 codeType=Integer 但 code 无法解析: " + v.code);
                }
            } else if ("Long".equals(codeType)) {
                try {
                    Long.parseLong(v.code);
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("列 " + c.columnName + " 枚举 codeType=Long 但 code 无法解析: " + v.code);
                }
            }
            v.codeLiteral = "String".equals(codeType)
                    ? "\"" + v.code.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
                    : "Long".equals(codeType) ? v.code + "L" : v.code;
        }
        c.enumColumn = true;
        c.enumClassName = cc.enumConfig.className;
        c.enumCodeType = codeType;
        c.enumValues = cc.enumConfig.values;
        c.modelType = c.enumClassName;
        c.conversion = "ENUM";
        c.toModelExpr = c.enumClassName + ".fromCode({do}." + getter + ")";
        c.toDoExpr = "ObjectUtil.isNull({model}." + getter + ") ? null : {model}." + getter + ".getCode()";
    }

    private static void applyJsonArrayConfig(TableMeta meta, ColumnMeta c, GeneratorConfig.ColumnConfig cc, String getter) {
        if (!"json".equals(c.dbType)) {
            throw new IllegalStateException("列 " + c.columnName + " type: jsonArray 只适用于 json 类型列");
        }
        String element = cc.javaObject == null ? "Object" : cc.javaObject;
        c.conversion = "JSON_ARRAY";
        c.jsonElementType = element;
        c.modelType = "List<" + shortType(element) + ">";
        c.toDoExpr = "JsonUtil.toJson({model}." + getter + ")";
        c.toModelExpr = element.contains("<")
                ? "JsonUtil.parseArray({do}." + getter + ", new TypeReference<java.util.List<" + element + ">>() {})"
                : "JsonUtil.parseArray({do}." + getter + ", " + element + ".class)";
    }

    private static void applyJsonObjectConfig(TableMeta meta, ColumnMeta c, GeneratorConfig.ColumnConfig cc, String getter) {
        if (!"json".equals(c.dbType)) {
            throw new IllegalStateException("列 " + c.columnName + " type: jsonObject 只适用于 json 类型列");
        }
        if ("String".equals(cc.javaObject)) {
            throw new IllegalStateException("列 " + c.columnName + " type: jsonObject 需要对象类型(POJO/Map)，原始字符串请用 type: json");
        }
        c.conversion = "JSON_OBJECT";
        c.toDoExpr = "JsonUtil.toJson({model}." + getter + ")";
        if (cc.javaObject == null) {
            c.modelType = "Map<String, Object>";
            c.toModelExpr = "JsonUtil.parseMap({do}." + getter + ")";
        } else if (cc.javaObject.contains("<")) {
            c.modelType = shortType(cc.javaObject);
            c.jsonElementType = cc.javaObject;
            c.toModelExpr = "JsonUtil.parseObject({do}." + getter + ", new TypeReference<" + cc.javaObject + ">() {})";
        } else {
            c.modelType = shortType(cc.javaObject);
            c.jsonElementType = cc.javaObject;
            c.toModelExpr = "JsonUtil.parseObject({do}." + getter + ", " + cc.javaObject + ".class)";
        }
    }

    /** 支持强制转换的 Java 类型（归一化后，与 GeneratorConfig.SUPPORTED_TYPES 一致）。 */
    private static final Set<String> COERCE_TYPES = Set.of(
            "Integer", "Long", "BigDecimal", "String",
            "Double", "Boolean", "Float", "Short", "Byte", "Character");

    private static void applyCoerceConfig(ColumnMeta c, String type, String getter) {
        if (COERCE_TYPES.contains(type)) {
            if (type.equals(c.javaType)) {
                // 同类型强制声明，直接赋值
                c.modelType = type;
                c.conversion = "NONE";
                return;
            }
            c.modelType = type;
            c.conversion = "COERCE";
            c.toModelExpr = convertCall(type) + "({do}." + getter + ")";
            c.toDoExpr = convertCall(c.javaType) + "({model}." + getter + ")";
            return;
        }
        if (type.equals(c.javaType)) {
            c.modelType = type;
            c.conversion = "NONE";
            return;
        }
        throw new IllegalStateException("不支持 " + c.javaType + "→" + type + " 转换（列 " + c.columnName
                + "），仅支持 Integer/Long/BigDecimal/String/Double/Boolean/Float/Short/Byte/Character 之间的强制转换与枚举/json 转换");
    }

    private static String convertCall(String type) {
        return switch (type) {
            case "Integer" -> "Convert.toInt";
            case "Long" -> "Convert.toLong";
            case "BigDecimal" -> "Convert.toBigDecimal";
            case "Double" -> "Convert.toDouble";
            case "Boolean" -> "Convert.toBool";
            case "Float" -> "Convert.toFloat";
            case "Short" -> "Convert.toShort";
            case "Byte" -> "Convert.toByte";
            case "Character" -> "Convert.toChar";
            default -> "Convert.toStr";
        };
    }

    /**
     * 全限定类型 → 声明用简单类型。泛型只缩短外层类型，内层保持原样，避免把内层 FQN 误拆。
     * 例：com.foo.Tag → Tag；com.foo.Map&lt;String, Object&gt; → Map&lt;String, Object&gt;；
     * List&lt;com.foo.Bar&gt; 外层无包名则原样返回。
     */
    static String shortType(String type) {
        if (type == null || !type.contains(".")) {
            return type;
        }
        int lt = type.indexOf('<');
        String head = lt >= 0 ? type.substring(0, lt) : type;
        String tail = lt >= 0 ? type.substring(lt) : "";
        int dot = head.lastIndexOf('.');
        return (dot >= 0 ? head.substring(dot + 1) : head) + tail;
    }

    /** 从全限定类型提取可 import 的外层类型（泛型只取头部）；无包名时返回 null。 */
    static String importableType(String type) {
        if (type == null) {
            return null;
        }
        int lt = type.indexOf('<');
        String head = lt >= 0 ? type.substring(0, lt) : type;
        return head.contains(".") ? head : null;
    }

    /**
     * 解析逻辑删除：表级 > 全局；enable 且列真实存在才启用；字符串列的值加引号。
     */
    private static void resolveLogicDelete(TableMeta meta, GeneratorConfig cfg,
                                           GeneratorConfig.TableConfig table, Map<String, String> rawColumns) {
        GeneratorConfig.LogicDeleteConfig ld = table.logicDelete != null ? table.logicDelete : cfg.globalLogicDelete;
        if (ld == null || !ld.enable) {
            return;
        }
        String rawType = rawColumns.get(ld.columnName);
        if (rawType == null) {
            return;
        }
        boolean stringColumn = "String".equals(mapJavaType(rawType));
        meta.logicDeleteEnabled = true;
        meta.logicDeleteColumn = ld.columnName;
        meta.logicDeleteNormal = sqlLiteral(ld.normalValue, stringColumn);
        meta.logicDeleteDelete = sqlLiteral(ld.deleteValue, stringColumn);
    }

    private static String sqlLiteral(String value, boolean quote) {
        if (value == null) {
            return "0";
        }
        return quote ? "'" + value.replace("'", "''") + "'" : value;
    }

    private static void buildQueryColumns(TableMeta table) {
        addQueryColumn(table, table.pkColumnName, table.pkPropertyName, table.pkJavaType, "主键");
        table.queryColumns.addAll(table.columns);
    }

    private static void addQueryColumn(TableMeta table, String columnName, String propertyName,
                                       String javaType, String comment) {
        ColumnMeta c = new ColumnMeta();
        c.columnName = columnName;
        c.propertyName = propertyName;
        c.javaType = javaType;
        c.modelType = javaType;
        c.modelString = "String".equals(javaType);
        c.comment = comment;
        c.string = "String".equals(javaType);
        c.queryType = queryType(c);
        table.queryColumns.add(c);
    }

    private static void buildSqlFragments(TableMeta table) {
        List<ColumnMeta> cols = table.columns;
        table.selectColumns = table.pkColumnName + ", " + cols.stream().map(c -> c.columnName).collect(Collectors.joining(", "))
                + ", create_time, update_time";

        // create_time/update_time 由数据库自动维护（DEFAULT CURRENT_TIMESTAMP / ON UPDATE），不参与 INSERT/UPDATE
        List<ColumnMeta> insertCols = cols.stream().filter(c -> !c.auto).collect(Collectors.toList());
        String insertColsSql = insertCols.stream().map(c -> c.columnName).collect(Collectors.joining(", "));
        String insertValsSql = insertCols.stream().map(c -> "#{" + c.propertyName + "}").collect(Collectors.joining(", "));
        // 主键自增由数据库生成；非自增主键（如 varchar）必须显式插入
        table.insertColumns = table.pkAuto
                ? insertColsSql
                : table.pkColumnName + (insertColsSql.isBlank() ? "" : ", " + insertColsSql);
        table.insertValues = table.pkAuto
                ? insertValsSql
                : "#{" + table.pkPropertyName + "}" + (insertValsSql.isBlank() ? "" : ", " + insertValsSql);

        List<ColumnMeta> updateCols = cols.stream().filter(c -> !c.auto && !c.pk).collect(Collectors.toList());
        table.updateSet = updateCols.stream().map(c -> c.columnName + " = #{" + c.propertyName + "}")
                .collect(Collectors.joining(",\n            "));
    }

    /** 原生基础类型归一化为包装类型（int→Integer 等），其余原样返回。 */
    private static String normalizeJavaType(String type) {
        return switch (type) {
            case "int" -> "Integer";
            case "long" -> "Long";
            case "boolean" -> "Boolean";
            case "double" -> "Double";
            case "float" -> "Float";
            case "short" -> "Short";
            case "byte" -> "Byte";
            case "char" -> "Character";
            default -> type;
        };
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
            case "json" -> "String";
            default -> "String";
        };
    }

    private static String cap(String name) {
        return name.isEmpty() ? name : Character.toUpperCase(name.charAt(0)) + name.substring(1);
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
