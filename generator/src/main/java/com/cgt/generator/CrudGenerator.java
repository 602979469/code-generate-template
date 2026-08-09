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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 表级 CRUD 生成：按配置的 tables 读表结构 -> 渲染 templates/table/*.ftl -> 写入目标项目。
 * 已存在 DO 的表默认跳过（防止覆盖），force_create=true 时强制覆盖。
 */
public final class CrudGenerator {

    /** 模板文件名 -> 目标相对路径（{pkg} 为包路径，{Class} 为类名）。 */
    private static final Map<String, String> TEMPLATES = new LinkedHashMap<>();

    static {
        TEMPLATES.put("{Class}DO.java.ftl", "common/dal/src/main/java/{pkg}/common/dal/dataobject/{Class}DO.java");
        TEMPLATES.put("{Class}Mapper.java.ftl", "common/dal/src/main/java/{pkg}/common/dal/mapper/{Class}Mapper.java");
        TEMPLATES.put("{Class}Mapper.xml.ftl", "common/dal/src/main/resources/mapper/{Class}Mapper.xml");
        TEMPLATES.put("{Class}.java.ftl", "core/model/src/main/java/{pkg}/core/model/domain/{Class}.java");
        TEMPLATES.put("{Class}QueryParam.java.ftl", "core/model/src/main/java/{pkg}/core/model/param/{Class}QueryParam.java");
        TEMPLATES.put("{Class}Repository.java.ftl", "core/repository/src/main/java/{pkg}/core/repository/{Class}Repository.java");
        TEMPLATES.put("{Class}RepositoryImpl.java.ftl", "core/repository/src/main/java/{pkg}/core/repository/impl/{Class}RepositoryImpl.java");
        TEMPLATES.put("{Class}RepositoryAssembler.java.ftl", "core/repository/src/main/java/{pkg}/core/repository/assembler/{Class}Assembler.java");
        TEMPLATES.put("{Class}DomainService.java.ftl", "core/service/src/main/java/{pkg}/core/service/{Class}DomainService.java");
        TEMPLATES.put("{Class}BizService.java.ftl", "biz/service-impl/src/main/java/{pkg}/app/biz/{Class}BizService.java");
        TEMPLATES.put("{Class}Controller.java.ftl", "web/src/main/java/{pkg}/app/web/controller/{Class}Controller.java");
        TEMPLATES.put("{Class}CreateRequest.java.ftl", "web/src/main/java/{pkg}/app/web/param/{Class}CreateRequest.java");
        TEMPLATES.put("{Class}UpdateRequest.java.ftl", "web/src/main/java/{pkg}/app/web/param/{Class}UpdateRequest.java");
        TEMPLATES.put("{Class}QueryRequest.java.ftl", "web/src/main/java/{pkg}/app/web/param/{Class}QueryRequest.java");
        TEMPLATES.put("{Class}Response.java.ftl", "web/src/main/java/{pkg}/app/web/result/{Class}Response.java");
        TEMPLATES.put("{Class}Assembler.java.ftl", "web/src/main/java/{pkg}/app/web/assembler/{Class}Assembler.java");
    }

    private final GeneratorConfig cfg;
    private final Configuration freemarker;

    public CrudGenerator(GeneratorConfig cfg) throws IOException {
        this.cfg = cfg;
        this.freemarker = new Configuration(Configuration.VERSION_2_3_33);
        this.freemarker.setDefaultEncoding("UTF-8");
        // 只启用 ${} 插值，MyBatis 的 #{...} 原样输出
        this.freemarker.setInterpolationSyntax(Configuration.DOLLAR_INTERPOLATION_SYNTAX);
        this.freemarker.setDirectoryForTemplateLoading(cfg.tableTemplatesDir().toFile());
        this.freemarker.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        this.freemarker.setLogTemplateExceptions(false);
    }

    public void run() throws IOException, TemplateException {
        if (cfg.tables.isEmpty()) {
            System.out.println("[gen] 未配置 tables，跳过表级生成");
            return;
        }
        for (GeneratorConfig.TableConfig table : cfg.tables) {
            TableMeta meta = DbMetaReader.read(cfg, table);

            // 跳过已创建的表：以 DO 文件是否存在判断，防止覆盖已经写好的文件
            Path doPath = cfg.outputDir.resolve("common/dal/src/main/java/" + cfg.packagePath()
                    + "/common/dal/dataobject/" + meta.className + "DO.java");
            if (Files.exists(doPath) && !table.forceCreate) {
                System.out.println("[gen] 跳过表 " + table.dbTableName + ": 已存在 "
                        + meta.className + "DO.java（如需覆盖请在配置中设 force_create: true，注意会覆盖手动修改的代码）");
                continue;
            }

            Map<String, Object> model = buildModel(meta);
            for (Map.Entry<String, String> entry : TEMPLATES.entrySet()) {
                render(meta, entry.getKey(), entry.getValue(), model, table.forceCreate);
            }
        }
    }

    private Map<String, Object> buildModel(TableMeta meta) {
        Map<String, Object> model = new LinkedHashMap<>();
        model.put("projectPrefix", cfg.projectPrefix);
        model.put("toolPrefix", cfg.toolPrefix);
        model.put("basePackage", cfg.basePackage());
        model.put("groupId", cfg.groupId);
        model.put("className", meta.className);
        model.put("classNameLower", meta.classNameLower);
        model.put("tableName", meta.tableName);
        model.put("tableComment", meta.tableComment);
        model.put("entityName", meta.entityName);
        model.put("columns", meta.columns);
        model.put("queryColumns", meta.queryColumns);
        model.put("requiredColumns", meta.requiredColumns);
        model.put("selectColumns", meta.selectColumns);
        model.put("insertColumns", meta.insertColumns);
        model.put("insertValues", meta.insertValues);
        model.put("updateSet", meta.updateSet);
        model.put("hasLocalDateTime", meta.hasLocalDateTime);
        model.put("hasLocalDate", meta.hasLocalDate);
        model.put("hasBigDecimal", meta.hasBigDecimal);
        model.put("hasRequiredString", meta.hasRequiredString);
        model.put("hasRequiredNonString", meta.hasRequiredNonString);
        return model;
    }

    private void render(TableMeta meta, String templateName, String outputPattern,
                        Map<String, Object> model, boolean force) throws IOException, TemplateException {
        Template template = freemarker.getTemplate(templateName);
        StringWriter writer = new StringWriter();
        template.process(model, writer);

        String targetRel = outputPattern
                .replace("{pkg}", cfg.packagePath())
                .replace("{Class}", meta.className);
        Path target = cfg.outputDir.resolve(targetRel);
        if (Files.exists(target) && !force) {
            System.out.println("[gen] 跳过(已存在): " + targetRel);
            return;
        }
        Files.createDirectories(target.getParent());
        Files.writeString(target, writer.toString(), StandardCharsets.UTF_8);
        System.out.println("[gen] 生成 " + targetRel);
    }
}
