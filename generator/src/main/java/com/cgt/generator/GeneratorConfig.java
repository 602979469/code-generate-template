package com.cgt.generator;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
    public String toolPrefix;
    public String groupId;
    public String projectArtifactPrefix;
    public String jdbcUrl;
    public String jdbcUsername;
    public String jdbcPassword;
    public Path outputDir;
    public final List<TableConfig> tables = new ArrayList<>();

    /**
     * 表配置项。
     *
     * <p>db_table_name：数据库表名（必填）；model_name：映射的 Java 对象名（必填，替代表名前缀剥离）；
     * force_create：默认 false，true 时强制覆盖该表已存在文件（危险，会覆盖手动修改的代码）。
     */
    public static final class TableConfig {
        public String dbTableName;
        public String modelName;
        public boolean forceCreate;
    }

    private GeneratorConfig() {
    }

    public String basePackage() {
        return groupId + "." + projectArtifactPrefix;
    }

    public String packagePath() {
        return basePackage().replace('.', '/');
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
        cfg.toolPrefix = str(root.get("toolPrefix"));
        cfg.groupId = str(root.get("groupId"));
        cfg.projectArtifactPrefix = str(root.get("projectArtifactPrefix"));

        if (root.get("jdbc") instanceof Map<?, ?> jdbc) {
            cfg.jdbcUrl = str(jdbc.get("url"));
            cfg.jdbcUsername = str(jdbc.get("username"));
            cfg.jdbcPassword = str(jdbc.get("password"));
        }

        String outputDir = str(root.get("outputDir"));
        cfg.outputDir = (outputDir == null || outputDir.isBlank() ? Path.of(".") : Path.of(outputDir))
                .toAbsolutePath().normalize();

        if (root.get("tables") instanceof List<?> list) {
            for (Object item : list) {
                if (!(item instanceof Map<?, ?> m)) {
                    continue;
                }
                TableConfig table = new TableConfig();
                table.dbTableName = str(m.get("db_table_name"));
                table.modelName = str(m.get("model_name"));
                Object force = m.get("force_create");
                table.forceCreate = force != null && Boolean.parseBoolean(String.valueOf(force));
                cfg.tables.add(table);
            }
        }
        return cfg;
    }

    /** 项目命名与 tables 校验。 */
    public void validateNaming() {
        StringBuilder missing = new StringBuilder();
        if (isBlank(projectPrefix)) {
            missing.append(" projectPrefix");
        }
        if (isBlank(toolPrefix)) {
            missing.append(" toolPrefix");
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
        if (!toolPrefix.matches("[A-Za-z][A-Za-z0-9]*")) {
            throw new IllegalArgumentException("toolPrefix 需为驼峰字母/数字(如 AiProd)");
        }
        if (!tables.isEmpty()) {
            if (isBlank(jdbcUrl) || isBlank(jdbcUsername)) {
                throw new IllegalArgumentException("配置了 tables 但缺少 jdbc.url / jdbc.username");
            }
            for (TableConfig table : tables) {
                if (isBlank(table.dbTableName)) {
                    throw new IllegalArgumentException("tables 中存在缺少 db_table_name 的配置项");
                }
                if (isBlank(table.modelName)) {
                    throw new IllegalArgumentException("tables 中存在缺少 model_name 的配置项(表: " + table.dbTableName + ")");
                }
                if (!table.modelName.matches("[A-Za-z][A-Za-z0-9]*")) {
                    throw new IllegalArgumentException("model_name 需为合法 Java 类名(如 User),当前: " + table.modelName);
                }
            }
        }
    }

    private static String str(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
