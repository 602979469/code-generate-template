package com.cgt.generator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

/**
 * 生成配置：读 generator.properties，命令行可覆盖。
 */
public final class GeneratorConfig {

    /** 配置文件所在目录，即模板仓库根目录（skeleton/、templates/ 相对它定位）。 */
    public Path repoDir;

    public String projectPrefix;
    /** 工具类/异常/常量前缀，默认取 projectPrefix。 */
    public String toolPrefix;
    public String groupId;
    public String projectArtifactPrefix;
    public String jdbcUrl;
    public String jdbcUsername;
    public String jdbcPassword;
    public String tablePrefix;
    public Path outputDir;

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

    public static GeneratorConfig load(Map<String, String> cli) {
        GeneratorConfig cfg = new GeneratorConfig();

        String configPath = cli.getOrDefault("c", "generator.properties");
        Path configFile = Path.of(configPath).toAbsolutePath().normalize();
        Properties props = new Properties();
        if (Files.exists(configFile)) {
            try (InputStream in = Files.newInputStream(configFile)) {
                props.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("读取配置失败: " + configFile, e);
            }
        }
        cfg.repoDir = configFile.getParent();

        cfg.projectPrefix = first(cli.get("p"), props.getProperty("projectPrefix"));
        cfg.toolPrefix = first(cli.get("tp"), props.getProperty("toolPrefix"));
        cfg.groupId = first(cli.get("g"), props.getProperty("groupId"));
        cfg.projectArtifactPrefix = first(cli.get("a"), props.getProperty("projectArtifactPrefix"));
        cfg.jdbcUrl = props.getProperty("jdbc.url",
                "jdbc:mysql://localhost:3306/aiplatform?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true");
        cfg.jdbcUsername = props.getProperty("jdbc.username", "root");
        cfg.jdbcPassword = props.getProperty("jdbc.password", "");
        cfg.tablePrefix = props.getProperty("tablePrefix", "");
        String outputDir = cli.get("o");
        if (outputDir == null || outputDir.isBlank()) {
            outputDir = props.getProperty("outputDir", ".");
        }
        cfg.outputDir = Path.of(outputDir).toAbsolutePath().normalize();
        return cfg;
    }

    /** 项目命名校验：init/table 必填，-a 需为合法 Java 包名后缀。 */
    public void validateNaming() {
        StringBuilder missing = new StringBuilder();
        if (isBlank(projectPrefix)) {
            missing.append(" -p");
        }
        if (isBlank(toolPrefix)) {
            missing.append(" -tp");
        }
        if (isBlank(groupId)) {
            missing.append(" -g");
        }
        if (isBlank(projectArtifactPrefix)) {
            missing.append(" -a");
        }
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("缺少必填参数:" + missing
                    + "\n项目命名全部必填,不设默认值。运行 gen.sh(无参数)查看用法。");
        }
        if (!projectArtifactPrefix.matches("[a-z][a-z0-9]*")) {
            throw new IllegalArgumentException("-a 需为小写字母/数字(如 aiprod),"
                    + "不能带连字符或大写——Java 包名不允许,如需连字符项目名请生成后自行调整 artifactId");
        }
        if (!isBlank(projectPrefix) && !projectPrefix.matches("[A-Za-z][A-Za-z0-9]*")) {
            throw new IllegalArgumentException("-p 需为驼峰字母/数字(如 AiProd)");
        }
        if (!isBlank(toolPrefix) && !toolPrefix.matches("[A-Za-z][A-Za-z0-9]*")) {
            throw new IllegalArgumentException("-tp 需为驼峰字母/数字(如 AiProd)");
        }
    }

    private static String first(String cli, String prop) {
        if (cli != null && !cli.isBlank()) {
            return cli;
        }
        if (prop != null && !prop.isBlank()) {
            return prop;
        }
        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
