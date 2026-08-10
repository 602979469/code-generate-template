package com.cgt.generator;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * generateExample 示例流程：
 * 1. 执行 skeleton/sql/example.sql 后台创建 example 表（CREATE TABLE IF NOT EXISTS，用户无需自己建表）；
 * 2. 生成 Tag / Profile 两个示例 POJO（jsonArray / jsonObject 绑定用，保证开箱可编译）；
 * 3. 提供内置 example 表配置（全量功能：枚举 / json / jsonArray / jsonObject / 强制转换 / 逻辑删除），
 *    注入普通表生成链路，只生成一个 Example 类。
 */
public final class ExampleGenerator {

    /** 内置示例表名（generateExample 自动建表并生成，用户 tables 中不可重复配置）。 */
    public static final String DEMO_TABLE = "example";

    private ExampleGenerator() {
    }

    /** 后台建表：执行骨架自带的 example.sql。 */
    public static void createTable(GeneratorConfig cfg) throws IOException {
        Path sqlFile = cfg.skeletonDir().resolve("sql/example.sql");
        if (!Files.exists(sqlFile)) {
            throw new IllegalStateException("缺少示例表 DDL: " + sqlFile);
        }
        String ddl = Files.readString(sqlFile, StandardCharsets.UTF_8)
                .lines()
                .filter(line -> !line.trim().startsWith("--"))
                .filter(line -> !line.isBlank())
                .reduce("", (a, b) -> a + "\n" + b);
        try (Connection conn = DriverManager.getConnection(cfg.jdbcUrl, cfg.jdbcUsername, cfg.jdbcPassword);
             Statement st = conn.createStatement()) {
            for (String statement : ddl.split(";")) {
                String s = statement.trim();
                if (!s.isBlank()) {
                    st.execute(s);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("创建示例表 example 失败（请检查数据库写权限）: " + e.getMessage(), e);
        }
        System.out.println("[gen] example(示例) 表创建成功");
    }

    /** 生成 Tag / Profile 示例 POJO，已存在则跳过。 */
    public static void generatePojos(GeneratorConfig cfg) throws IOException, TemplateException {
        Configuration freemarker = new Configuration(Configuration.VERSION_2_3_33);
        freemarker.setDefaultEncoding("UTF-8");
        freemarker.setNumberFormat("0");
        freemarker.setInterpolationSyntax(Configuration.DOLLAR_INTERPOLATION_SYNTAX);
        freemarker.setDirectoryForTemplateLoading(cfg.repoDir.resolve("templates/example").toFile());
        freemarker.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        freemarker.setLogTemplateExceptions(false);

        Map<String, Object> model = new LinkedHashMap<>();
        model.put("basePackage", cfg.basePackage());
        for (String pojo : List.of("Tag", "Profile")) {
            Template template = freemarker.getTemplate(pojo + ".java.ftl");
            StringWriter writer = new StringWriter();
            template.process(model, writer);
            Path target = cfg.outputDir.resolve("core/model/src/main/java/" + cfg.packagePath()
                    + "/core/model/domain/" + pojo + ".java");
            if (Files.exists(target)) {
                continue;
            }
            Files.createDirectories(target.getParent());
            Files.writeString(target, writer.toString(), StandardCharsets.UTF_8);
        }
    }

    /** 内置示例表配置：表结构见 skeleton/sql/example.sql，列配置覆盖全部演示点。 */
    public static GeneratorConfig.TableConfig exampleTableConfig(String basePackage) {
        GeneratorConfig.TableConfig t = new GeneratorConfig.TableConfig();
        t.dbTableName = DEMO_TABLE;
        t.modelName = "Example";
        t.modelComment = "示例";
        t.example = true;
        t.generateController = true;

        GeneratorConfig.LogicDeleteConfig ld = new GeneratorConfig.LogicDeleteConfig();
        ld.enable = true;
        ld.columnName = "del_flag";
        ld.normalValue = "0";
        ld.deleteValue = "1";
        t.logicDelete = ld;

        t.columns.put("user_type", enumConfig("UserTypeEnum", "Integer",
                List.of(new String[]{"0", "SYSTEM_USER", "系统用户"}, new String[]{"1", "NORMAL_USER", "普通用户"})));
        t.columns.put("status", enumConfig("UserStatusEnum", "String",
                List.of(new String[]{"ENABLED", "ENABLED", "启用"}, new String[]{"DISABLED", "DISABLED", "停用"})));

        GeneratorConfig.ColumnConfig loginCount = new GeneratorConfig.ColumnConfig();
        loginCount.type = "Integer";
        t.columns.put("login_count", loginCount);

        GeneratorConfig.ColumnConfig tags = new GeneratorConfig.ColumnConfig();
        tags.type = "jsonArray";
        tags.javaObject = basePackage + ".core.model.domain.Tag";
        t.columns.put("tags", tags);

        GeneratorConfig.ColumnConfig profile = new GeneratorConfig.ColumnConfig();
        profile.type = "jsonObject";
        profile.javaObject = basePackage + ".core.model.domain.Profile";
        t.columns.put("profile", profile);
        return t;
    }

    private static GeneratorConfig.ColumnConfig enumConfig(String className, String codeType, List<String[]> values) {
        GeneratorConfig.ColumnConfig cc = new GeneratorConfig.ColumnConfig();
        cc.type = "enum";
        cc.enumConfig = new GeneratorConfig.EnumConfig();
        cc.enumConfig.className = className;
        cc.enumConfig.codeType = codeType;
        for (String[] v : values) {
            GeneratorConfig.EnumValue ev = new GeneratorConfig.EnumValue();
            ev.code = v[0];
            ev.name = v[1];
            ev.desc = v[2];
            cc.enumConfig.values.add(ev);
        }
        return cc;
    }
}
