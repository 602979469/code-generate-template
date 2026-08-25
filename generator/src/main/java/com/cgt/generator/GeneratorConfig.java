package com.cgt.generator;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 生成配置：从 YAML 配置文件加载（gen.sh 只传一个配置文件路径）。
 * 项目命名全部必填、无默认值；tables 为对象列表。
 */
public final class GeneratorConfig {

    /** 模板仓库根目录（skeleton/、templates/ 相对它定位），由 gen.sh 通过 -Dcgt.templateRepo 传入。 */
    public Path repoDir;

    public String projectPrefix;
    public String groupId;
    public String projectArtifactPrefix;
    /** 显式基础包名（2.0 配置项）；缺省 = groupId + "." + projectArtifactPrefix。 */
    public String basePackageOverride;
    /** 项目风格：monolith | microservice（默认 monolith；microservice 暂未支持）。 */
    public String projectStyle = "monolith";
    /** 模块样式：flat | aggregated | maven-module（默认 flat）。 */
    public String moduleLayout = "flat";
    /** 业务模块列表（2.0 配置项，供表级 module 关联）。 */
    public final List<ModuleConfig> modules = new ArrayList<>();
    public String jdbcUrl;
    public String jdbcUsername;
    public String jdbcPassword;
    public Path outputDir;
    /** 全局逻辑删除配置，null 表示未配置。 */
    public LogicDeleteConfig globalLogicDelete;
    /** 全局敏感列名：生成时从查询参数/响应/查询条件中剔除（password 等按默认名单识别，此处可追加）。 */
    public final List<TableConfig> tables = new ArrayList<>();

    /**
     * 表配置项。
     *
     * <p>db_table_name：数据库表名（必填）；model_name：映射的 Java 对象名（必填，替代表名前缀剥离）；
     * model_comment：中文实体名（必填，如 sys_user -> 用户），生成代码的所有注释都用它拼接；
     * force_create：默认 false，true 时强制覆盖该表已存在文件（危险，会覆盖手动修改的代码）。
     * generateController：默认 true，false = 内部表不生成 Controller 及 web 专属文件；
     * logicDelete：表级逻辑删除配置，覆盖全局 globalLogicDelete；
     * columns：列级配置，键为数据库列名，未配置的列按默认映射。
     */
    public static final class TableConfig {
        public String dbTableName;
        public String modelName;
        public String modelComment;
        /** 所属业务模块（2.0 配置项，与 modules[].name 匹配，可空）。 */
        public String module;
        public boolean forceCreate;
        public boolean generateController = true;
        public LogicDeleteConfig logicDelete;
        public final Map<String, ColumnConfig> columns = new LinkedHashMap<>();
    }

    /** 业务模块配置。 */
    public static final class ModuleConfig {
        public String name;
        public String displayName;
    }

    /** 逻辑删除配置。 */
    public static final class LogicDeleteConfig {
        public boolean enable;
        public String columnName;
        public String normalValue;
        public String deleteValue;
    }

    /** 列级配置：type 显式声明转换逻辑。 */
    public static final class ColumnConfig {
        /** enum / json / jsonArray / jsonObject / Java 类型（如 Integer）。 */
        public String type;
        /** 列别名（javadoc/注释用），覆盖数据库列注释。 */
        public String comment;
        /** 敏感列：不进查询参数/响应（默认按列名识别 password/token 等，可显式声明）。 */
        public boolean sensitive;
        /** 脱敏策略（列级）：PHONE / ID_CARD / BANK_CARD / EMAIL / NAME / ADDRESS / PASSWORD / NONE。 */
        public String sensitiveStrategy;
        /** jsonArray 元素类型 / jsonObject 目标类型（全限定类名）。 */
        public String javaObject;
        /** type: enum 时的枚举配置。 */
        public EnumConfig enumConfig;
    }

    /** 枚举配置。 */
    public static final class EnumConfig {
        public String className;
        public final List<EnumValue> values = new ArrayList<>();
    }

    /** 枚举值。 */
    public static final class EnumValue {
        public String code;
        public String name;
        public String desc;
        /** 代码字面量（String 加引号转义，数值原样），由 DbMetaReader 计算。 */
        public String codeLiteral;

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }

        public String getCodeLiteral() {
            return codeLiteral;
        }
    }

    private GeneratorConfig() {
    }

    public String basePackage() {
        if (basePackageOverride != null && !basePackageOverride.isBlank()) {
            return basePackageOverride;
        }
        return groupId + "." + projectArtifactPrefix;
    }

    public String packagePath() {
        return basePackage().replace('.', '/');
    }

    /** 布局策略路由：projectStyle + moduleLayout → 策略类；未配置 = monolith:flat。 */
    public LayoutStrategy layoutStrategy() {
        if ("microservice".equals(projectStyle)) {
            throw new IllegalArgumentException("projectStyle=microservice 暂未支持（v1 仅 monolith）");
        }
        return switch (moduleLayout) {
            case "aggregated" -> new AggregatedStrategy(projectArtifactPrefix);
            case "maven-module" -> new MavenModuleStrategy(projectArtifactPrefix);
            case "flat" -> new FlatStrategy();
            default -> throw new IllegalArgumentException("不支持的 moduleLayout: " + moduleLayout
                    + "（支持 flat / aggregated / maven-module）");
        };
    }

    public Path skeletonDir() {
        return repoDir.resolve("skeleton");
    }

    public Path tableTemplatesDir() {
        return repoDir.resolve("templates").resolve("table");
    }

    public static GeneratorConfig load(String configPath) {
        Path configFile = Path.of(configPath).toAbsolutePath().normalize();
        if (!Files.exists(configFile)) {
            throw new IllegalArgumentException("配置文件不存在: " + configFile);
        }

        GeneratorConfig cfg = new GeneratorConfig();
        String repo = System.getProperty("cgt.templateRepo");
        cfg.repoDir = Path.of(repo == null || repo.isBlank() ? configFile.getParent().toString() : repo)
                .toAbsolutePath().normalize();

        Map<String, Object> root;
        try (InputStream in = Files.newInputStream(configFile)) {
            root = new Yaml().load(in);
        } catch (IOException e) {
            throw new IllegalStateException("读取配置文件失败: " + configFile, e);
        }
        if (root == null) {
            root = Map.of();
        }

        cfg.projectPrefix = str(root.get("projectPrefix"));
        cfg.groupId = str(root.get("groupId"));
        cfg.projectArtifactPrefix = str(root.get("projectArtifactPrefix"));
        cfg.basePackageOverride = str(root.get("basePackage"));
        cfg.projectStyle = defaultStr(root.get("projectStyle"), "monolith");
        cfg.moduleLayout = defaultStr(root.get("moduleLayout"), "flat");

        if (root.get("modules") instanceof List<?> moduleList) {
            for (Object item : moduleList) {
                if (!(item instanceof Map<?, ?> m)) {
                    continue;
                }
                ModuleConfig mc = new ModuleConfig();
                mc.name = str(m.get("name"));
                mc.displayName = str(m.get("displayName"));
                if (mc.name != null && !mc.name.isBlank()) {
                    cfg.modules.add(mc);
                }
            }
        }

        if (root.get("jdbc") instanceof Map<?, ?> jdbc) {
            cfg.jdbcUrl = str(jdbc.get("url"));
        cfg.jdbcUsername = str(jdbc.get("username"));
        cfg.jdbcPassword = str(jdbc.get("password"));
        }

        cfg.globalLogicDelete = parseLogicDelete(root.get("globalLogicDelete"));

        String outputDir = str(root.get("outputDir"));
        cfg.outputDir = (outputDir == null || outputDir.isBlank() ? Path.of(".") : Path.of(expandHome(outputDir)))
                .toAbsolutePath().normalize();

        if (root.get("tables") instanceof List<?> list) {
            for (Object item : list) {
                if (!(item instanceof Map<?, ?> m)) {
                    continue;
                }
                TableConfig table = new TableConfig();
                table.dbTableName = str(m.get("db_table_name"));
                // model_name 缺省 = 表名驼峰（如 example_inner -> ExampleInner）
                String modelName = str(m.get("model_name"));
                table.modelName = isBlank(modelName) ? DbMetaReader.toUpperCamel(table.dbTableName) : modelName;
                table.modelComment = str(m.get("model_comment"));
                table.module = str(m.get("module"));
                Object force = m.get("force_create");
                table.forceCreate = force != null && Boolean.parseBoolean(String.valueOf(force));
                Object controller = m.get("generateController");
                table.generateController = controller == null || Boolean.parseBoolean(String.valueOf(controller));
                table.logicDelete = parseLogicDelete(m.get("logicDelete"));
                if (m.get("columns") instanceof Map<?, ?> columns) {
                    for (Map.Entry<?, ?> entry : columns.entrySet()) {
                        if (!(entry.getValue() instanceof Map<?, ?> col)) {
                            continue;
                        }
                        ColumnConfig cc = parseColumnConfig(col);
                        table.columns.put(String.valueOf(entry.getKey()), cc);
                    }
                }
                cfg.tables.add(table);
            }
        }
        return cfg;
    }

    /** 展开配置里的 ~ 前缀为用户主目录（~/AiProd -> /Users/xxx/AiProd）。 */
    private static String expandHome(String value) {
        if (value != null && value.startsWith("~/")) {
            return System.getProperty("user.home") + value.substring(1);
        }
        return value;
    }

    private static LogicDeleteConfig parseLogicDelete(Object value) {
        if (!(value instanceof Map<?, ?> m)) {
            return null;
        }
        LogicDeleteConfig cfg = new LogicDeleteConfig();
        Object enable = m.get("enable");
        cfg.enable = enable != null && Boolean.parseBoolean(String.valueOf(enable));
        cfg.columnName = str(m.get("column_name"));
        cfg.normalValue = valueOf(m.get("normal_value"));
        cfg.deleteValue = valueOf(m.get("delete_value"));
        return cfg;
    }

    private static ColumnConfig parseColumnConfig(Map<?, ?> col) {
        ColumnConfig cc = new ColumnConfig();
        cc.type = str(col.get("type"));
        cc.comment = str(col.get("comment"));
        cc.javaObject = str(col.get("javaObject"));
        cc.sensitive = "true".equalsIgnoreCase(str(col.get("sensitive")));
        cc.sensitiveStrategy = str(col.get("sensitiveStrategy"));
        if (col.get("enum") instanceof Map<?, ?> enumCfg) {
            EnumConfig ec = new EnumConfig();
            ec.className = str(enumCfg.get("className"));
            if (enumCfg.get("values") instanceof List<?> values) {
                for (Object item : values) {
                    if (!(item instanceof Map<?, ?> v)) {
                        continue;
                    }
                    EnumValue ev = new EnumValue();
                    ev.code = valueOf(v.get("code"));
                    ev.name = str(v.get("name"));
                    ev.desc = str(v.get("desc"));
                    ec.values.add(ev);
                }
            }
            cc.enumConfig = ec;
        }
        return cc;
    }

    /** 逻辑删除值可能是数字或字符串，统一转字符串（SQL 拼装时按列类型决定是否加引号）。 */
    private static String valueOf(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /** 项目命名与 tables 校验。 */
    public void validateNaming() {
        StringBuilder missing = new StringBuilder();
        if (isBlank(projectPrefix)) {
            missing.append(" projectPrefix");
        }
        if (isBlank(groupId)) {
            missing.append(" groupId");
        }
        if (isBlank(projectArtifactPrefix)) {
            missing.append(" projectArtifactPrefix");
        }
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("配置缺少必填项:" + missing
                    + "\n请查看 generate.yaml 配置模板(运行 ./gen.sh 可生成),项目命名全部必填、无默认值。");
        }
        if (!projectArtifactPrefix.matches("[a-z][a-z0-9]*")) {
            throw new IllegalArgumentException("projectArtifactPrefix 需为小写字母/数字(如 aiprod),"
                    + "不能带连字符或大写——Java 包名不允许,如需连字符项目名请生成后自行调整 artifactId");
        }
        if (!projectPrefix.matches("[A-Za-z][A-Za-z0-9]*")) {
            throw new IllegalArgumentException("projectPrefix 需为驼峰字母/数字(如 AiProd)");
        }
        if (!"monolith".equals(projectStyle)) {
            throw new IllegalArgumentException("projectStyle 仅支持 monolith（microservice 暂未实现，配置到即报错）");
        }
        if (!"flat".equals(moduleLayout)) {
            throw new IllegalArgumentException("moduleLayout 仅支持 flat（本期只做 flat，aggregated / maven-module 暂未实现）");
        }
        if (!tables.isEmpty()) {
            if (isBlank(jdbcUrl) || isBlank(jdbcUsername)) {
                throw new IllegalArgumentException("配置了 tables 但缺少 jdbc.url / jdbc.username");
            }
        }
        validateLogicDelete("globalLogicDelete", globalLogicDelete);
        if (!tables.isEmpty()) {
            for (TableConfig table : tables) {
                if (isBlank(table.dbTableName)) {
                    throw new IllegalArgumentException("tables 中存在缺少 db_table_name 的配置项");
                }
                if (!isBlank(table.modelName) && !table.modelName.matches("[A-Za-z][A-Za-z0-9]*")) {
                    throw new IllegalArgumentException("model_name 需为合法 Java 类名(如 User),当前: " + table.modelName);
                }
                validateLogicDelete("tables[" + table.dbTableName + "].logicDelete", table.logicDelete);
                for (Map.Entry<String, ColumnConfig> entry : table.columns.entrySet()) {
                    validateColumnConfig(table.dbTableName, entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private static void validateLogicDelete(String name, LogicDeleteConfig cfg) {
        if (cfg == null || !cfg.enable) {
            return;
        }
        StringBuilder missing = new StringBuilder();
        if (isBlank(cfg.columnName)) {
            missing.append(" column_name");
        }
        if (isBlank(cfg.normalValue)) {
            missing.append(" normal_value");
        }
        if (isBlank(cfg.deleteValue)) {
            missing.append(" delete_value");
        }
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(name + " 的 enable 为 true 时以下配置项必填:" + missing);
        }
    }

    private static void validateColumnConfig(String tableName, String columnName, ColumnConfig cc) {
        if (isBlank(cc.type)) {
            // 2.0：允许只配 sensitive / comment 的列（type 缺省走数据库默认映射）
            return;
        }
        if ("enum".equals(cc.type)) {
            if (cc.enumConfig == null) {
                throw new IllegalArgumentException("表 " + tableName + " 列 " + columnName + " type: enum 时必须配置 enum 块");
            }
            if (isBlank(cc.enumConfig.className)) {
                throw new IllegalArgumentException("表 " + tableName + " 列 " + columnName + " 枚举缺少 className");
            }
            if (!cc.enumConfig.className.matches("[A-Za-z][A-Za-z0-9]*")) {
                throw new IllegalArgumentException("表 " + tableName + " 列 " + columnName + " 枚举 className 需为合法 Java 类名: "
                        + cc.enumConfig.className);
            }
            if (cc.enumConfig.values.isEmpty()) {
                throw new IllegalArgumentException("表 " + tableName + " 列 " + columnName + " 枚举缺少 values");
            }
            java.util.Set<String> codes = new java.util.HashSet<>();
            java.util.Set<String> names = new java.util.HashSet<>();
            for (EnumValue v : cc.enumConfig.values) {
                if (isBlank(v.code) || isBlank(v.name) || isBlank(v.desc)) {
                    throw new IllegalArgumentException("表 " + tableName + " 列 " + columnName
                            + " 枚举 value 的 code/name/desc 均必填");
                }
                if (!v.name.matches("[A-Za-z_][A-Za-z0-9_]*")) {
                    throw new IllegalArgumentException("表 " + tableName + " 列 " + columnName
                            + " 枚举 name 需为合法 Java 标识符: " + v.name);
                }
                if (!codes.add(v.code)) {
                    throw new IllegalArgumentException("表 " + tableName + " 列 " + columnName
                            + " 枚举 code 重复: " + v.code);
                }
                if (!names.add(v.name)) {
                    throw new IllegalArgumentException("表 " + tableName + " 列 " + columnName
                            + " 枚举 name 重复: " + v.name);
                }
            }
        } else if (!"json".equals(cc.type) && !"jsonArray".equals(cc.type) && !"jsonObject".equals(cc.type)) {
            // 集合类型只对 json 列有意义
            if (isCollectionType(cc.type)) {
                throw new IllegalArgumentException("表 " + tableName + " 列 " + columnName
                        + " 集合类型仅用于 json 列（type: jsonArray / jsonObject），普通列不支持: " + cc.type);
            }
            // Java 类型覆盖：只允许转换矩阵内的类型；其余按文档口径报"不是支持的值"
            if (!SUPPORTED_TYPES.contains(cc.type)) {
                throw new IllegalArgumentException("表 " + tableName + " 列 " + columnName
                        + " type 不是支持的值(enum/json/jsonArray/jsonObject/Integer/Long/BigDecimal/String/Double"
                        + "/Boolean/Float/Short/Byte/Character): " + cc.type);
            }
        }
        if (cc.javaObject != null && !cc.javaObject.matches("[A-Za-z_$][A-Za-z0-9_$<>., ]*")) {
            throw new IllegalArgumentException("表 " + tableName + " 列 " + columnName
                    + " javaObject 需为合法 Java 类型(全限定类名或泛型): " + cc.javaObject);
        }
        if ("jsonObject".equals(cc.type) && "String".equals(cc.javaObject)) {
            throw new IllegalArgumentException("表 " + tableName + " 列 " + columnName
                    + " type: jsonObject 需要对象类型(POJO/Map)，原始字符串请用 type: json");
        }
    }

    /** 支持直接作为 type 的 Java 类型（含归一化前的原生类型）。 */
    private static final java.util.Set<String> SUPPORTED_TYPES = java.util.Set.of(
            "Integer", "Long", "BigDecimal", "String",
            "Double", "Boolean", "Float", "Short", "Byte", "Character",
            "int", "long", "boolean", "double", "float", "short", "byte", "char");

    private static boolean isCollectionType(String type) {
        return type.contains("<") || type.startsWith("List") || type.startsWith("Map") || type.startsWith("Set");
    }

    private static String str(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private static String defaultStr(Object value, String def) {
        String s = str(value);
        return s == null || s.isBlank() ? def : s;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
