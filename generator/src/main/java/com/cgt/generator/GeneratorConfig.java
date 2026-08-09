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

        cfg.projectPrefix = first(cli.get("p"), props.getProperty("projectPrefix"), "AiProd");
        cfg.toolPrefix = first(cli.get("toolPrefix"), props.getProperty("toolPrefix"), cfg.projectPrefix);
        cfg.groupId = first(cli.get("g"), props.getProperty("groupId"), "com.jakt");
        cfg.projectArtifactPrefix = first(cli.get("a"), props.getProperty("projectArtifactPrefix"),
                cfg.projectPrefix.toLowerCase());
        cfg.jdbcUrl = props.getProperty("jdbc.url",
                "jdbc:mysql://localhost:3306/aiplatform?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true");
        cfg.jdbcUsername = props.getProperty("jdbc.username", "root");
        cfg.jdbcPassword = props.getProperty("jdbc.password", "");
        cfg.tablePrefix = props.getProperty("tablePrefix", "");
        cfg.outputDir = Path.of(first(cli.get("o"), props.getProperty("outputDir"), ".")).toAbsolutePath().normalize();
        return cfg;
    }

    private static String first(String cli, String prop, String def) {
        if (cli != null && !cli.isBlank()) {
            return cli;
        }
        if (prop != null && !prop.isBlank()) {
            return prop;
        }
        return def;
    }
}
