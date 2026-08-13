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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 表级 CRUD 生成：按配置的 tables 读表结构 -> 渲染 templates/table/*.ftl -> 写入目标项目。
 * 每张表独立判定 成功/跳过/强制覆盖；结束时输出执行报告框。
 */
public final class CrudGenerator {

    /** 模板文件名 -> 目标相对路径（{pkg} 为包路径，{Class} 为类名）。 */
    private static final Map<String, String> TEMPLATES = new LinkedHashMap<>();

    /** generateController: false 时不生成的 web 专属模板。 */
    private static final Set<String> WEB_TEMPLATES = Set.of(
            "{Class}Controller.java.ftl",
            "{Class}CreateRequest.java.ftl",
            "{Class}UpdateRequest.java.ftl",
            "{Class}QueryRequest.java.ftl",
            "{Class}Response.java.ftl",
            "{Class}Assembler.java.ftl",
            "{Class}ParamChecker.java.ftl"
    );

    /** generateController: false 时不生成的 biz 专属模板（内部表无对外接口，Manager 无存在必要）。 */
    private static final Set<String> BIZ_TEMPLATES = Set.of(
            "{Class}Manager.java.ftl",
            "{Class}ManagerImpl.java.ftl"
    );

    static {
        TEMPLATES.put("{Class}DO.java.ftl", "common/dal/src/main/java/{pkg}/common/dal/dataobject/{Class}DO.java");
        TEMPLATES.put("{Class}Mapper.java.ftl", "common/dal/src/main/java/{pkg}/common/dal/mapper/{Class}Mapper.java");
        TEMPLATES.put("{Class}Mapper.xml.ftl", "common/dal/src/main/resources/mapper/{Class}Mapper.xml");
        TEMPLATES.put("{Class}.java.ftl", "core/model/src/main/java/{pkg}/core/model/domain/{Class}.java");
        TEMPLATES.put("{Class}QueryParam.java.ftl", "core/model/src/main/java/{pkg}/core/model/param/{Class}QueryParam.java");
        TEMPLATES.put("{Class}Repository.java.ftl", "core/repository/src/main/java/{pkg}/core/repository/{Class}Repository.java");
        TEMPLATES.put("{Class}RepositoryImpl.java.ftl", "core/repository/src/main/java/{pkg}/core/repository/impl/{Class}RepositoryImpl.java");
        TEMPLATES.put("{Class}Convertor.java.ftl", "core/repository/src/main/java/{pkg}/core/repository/convertor/{Class}Convertor.java");
        TEMPLATES.put("{Class}Service.java.ftl", "core/service/src/main/java/{pkg}/core/service/{Class}Service.java");
        TEMPLATES.put("{Class}ServiceImpl.java.ftl", "core/service/src/main/java/{pkg}/core/service/impl/{Class}ServiceImpl.java");
        TEMPLATES.put("{Class}Manager.java.ftl", "biz/service-impl/src/main/java/{pkg}/biz/service/{Class}Manager.java");
        TEMPLATES.put("{Class}ManagerImpl.java.ftl", "biz/service-impl/src/main/java/{pkg}/biz/service/impl/{Class}ManagerImpl.java");
        TEMPLATES.put("{Class}ParamChecker.java.ftl", "web/src/main/java/{pkg}/web/checker/{Class}ParamChecker.java");
        TEMPLATES.put("{Class}Controller.java.ftl", "web/src/main/java/{pkg}/web/controller/{Class}Controller.java");
        TEMPLATES.put("{Class}CreateRequest.java.ftl", "web/src/main/java/{pkg}/web/param/{Class}CreateRequest.java");
        TEMPLATES.put("{Class}UpdateRequest.java.ftl", "web/src/main/java/{pkg}/web/param/{Class}UpdateRequest.java");
        TEMPLATES.put("{Class}QueryRequest.java.ftl", "web/src/main/java/{pkg}/web/param/{Class}QueryRequest.java");
        TEMPLATES.put("{Class}Response.java.ftl", "web/src/main/java/{pkg}/web/result/{Class}Response.java");
        TEMPLATES.put("{Class}Assembler.java.ftl", "web/src/main/java/{pkg}/web/assembler/{Class}Assembler.java");
    }

    private final GeneratorConfig cfg;
    private final Configuration freemarker;

    public CrudGenerator(GeneratorConfig cfg) throws IOException {
        this.cfg = cfg;
        this.freemarker = new Configuration(Configuration.VERSION_2_3_33);
        this.freemarker.setDefaultEncoding("UTF-8");
        // 数字不输出千分位（如 2000 而非 2,000），避免生成 @Size(max = 2,000) 这类非法 Java 代码
        this.freemarker.setNumberFormat("0");
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
        System.out.println("[gen] 开始生成 " + cfg.tables.size() + " 张表");

        int success = 0;
        int skipped = 0;
        int warned = 0;
        List<String> skipReasons = new ArrayList<>();
        List<String> warnReasons = new ArrayList<>();

        for (GeneratorConfig.TableConfig table : cfg.tables) {
            TableMeta meta = DbMetaReader.read(cfg, table);
            String display = table.dbTableName;

            // 表级跳过判定：DO 已存在 / 枚举已存在（未 force_create）
            String skipReason = null;
            Path doPath = cfg.outputDir.resolve("common/dal/src/main/java/" + cfg.packagePath()
                    + "/common/dal/dataobject/" + meta.className + "DO.java");
            if (Files.exists(doPath) && !table.forceCreate) {
                skipReason = "DO 已存在";
            }
            if (skipReason == null) {
                String enumFile = firstExistingEnumFile(meta);
                if (enumFile != null && !table.forceCreate) {
                    skipReason = "枚举 " + enumFile + " 已存在";
                }
            }
            if (skipReason != null) {
                System.out.println("[gen] " + display + " 表已存在，跳过（" + skipReason
                        + "；如需覆盖请配置 force_create: true）");
                skipped++;
                skipReasons.add(display + ": " + skipReason);
                continue;
            }

            boolean overwriting = table.forceCreate && (Files.exists(doPath) || firstExistingEnumFile(meta) != null);
            if (overwriting) {
                System.out.println("[gen] ⚠️ " + display + " 表存在，强制覆盖（会覆盖手动修改的代码！）");
                warned++;
                warnReasons.add(display + ": 已存在，force_create 强制覆盖");
            }

            Map<String, Object> model = buildModel(meta);
            int fileCount = 0;
            for (Map.Entry<String, String> entry : TEMPLATES.entrySet()) {
                if (!table.generateController
                        && (WEB_TEMPLATES.contains(entry.getKey()) || BIZ_TEMPLATES.contains(entry.getKey()))) {
                    continue;
                }
                if (render(meta, entry.getKey(), entry.getValue(), model, table.forceCreate)) {
                    fileCount++;
                }
            }
            fileCount += renderEnums(meta, table.forceCreate);
            generateTableSql(table, table.forceCreate);

            System.out.println("[gen] " + display + " 表代码生成成功（" + fileCount + " 个文件）");
            success++;
        }

        if (skipped > 0 || warned > 0) {
            printReport(success, skipped, warned, skipReasons, warnReasons);
        } else {
            System.out.println("[gen] 全部 " + cfg.tables.size() + " 张表生成成功");
        }
    }

    private String firstExistingEnumFile(TableMeta meta) {
        for (ColumnMeta c : meta.columns) {
            if (c.enumColumn) {
                Path p = cfg.outputDir.resolve("core/model/src/main/java/" + cfg.packagePath()
                        + "/core/model/enums/" + c.enumClassName + ".java");
                if (Files.exists(p)) {
                    return c.enumClassName;
                }
            }
        }
        return null;
    }

    /** 渲染枚举模板，返回生成/覆盖的文件数（已存在且非 force 时跳过）。 */
    private int renderEnums(TableMeta meta, boolean force) throws IOException, TemplateException {
        Set<String> seen = new LinkedHashSet<>();
        int count = 0;
        for (ColumnMeta c : meta.columns) {
            if (!c.enumColumn || !seen.add(c.enumClassName)) {
                continue;
            }
            Map<String, Object> enumModel = new LinkedHashMap<>();
            enumModel.put("basePackage", cfg.basePackage());
            enumModel.put("entityName", meta.entityName);
            enumModel.put("enumDesc", c.comment);
            enumModel.put("enumClassName", c.enumClassName);
            enumModel.put("enumCodeType", c.enumCodeType);
            enumModel.put("enumValues", c.enumValues);

            Template template = freemarker.getTemplate("{EnumName}.java.ftl");
            StringWriter writer = new StringWriter();
            template.process(enumModel, writer);
            Path target = cfg.outputDir.resolve("core/model/src/main/java/" + cfg.packagePath()
                    + "/core/model/enums/" + c.enumClassName + ".java");
            if (Files.exists(target) && !force) {
                continue;
            }
            Files.createDirectories(target.getParent());
            Files.writeString(target, writer.toString(), StandardCharsets.UTF_8);
            count++;
        }
        return count;
    }

    /**
     * 每张配置的表生成一个独立 SQL 文件（sql/{db_table_name}.sql），内容为该表的 SHOW CREATE TABLE 真实 DDL。
     * 已存在的文件静默跳过（force_create=true 时覆盖）。
     */
    private void generateTableSql(GeneratorConfig.TableConfig table, boolean force) throws IOException {
        String ddl = DbMetaReader.readCreateTable(cfg, table);
        if (ddl == null || ddl.isBlank()) {
            return;
        }
        Path sqlFile = cfg.outputDir.resolve("sql").resolve(table.dbTableName + ".sql");
        if (Files.exists(sqlFile) && !force) {
            return;
        }
        Files.createDirectories(sqlFile.getParent());
        String content = "-- ------------------------------------------------------------------\n"
                + "-- 表: " + table.dbTableName + "（由 code-generate-template 按 tables 配置生成）\n"
                + "-- ------------------------------------------------------------------\n"
                + ddl.trim() + (ddl.trim().endsWith(";") ? "" : ";") + "\n";
        Files.writeString(sqlFile, content, StandardCharsets.UTF_8);
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
        model.put("hasString", meta.hasString);
        model.put("logicDeleteEnabled", meta.logicDeleteEnabled);
        model.put("logicDeleteColumn", meta.logicDeleteColumn);
        model.put("logicDeleteNormal", meta.logicDeleteNormal);
        model.put("logicDeleteDelete", meta.logicDeleteDelete);
        model.put("pkColumnName", meta.pkColumnName);
        model.put("pkPropertyName", meta.pkPropertyName);
        model.put("pkJavaType", meta.pkJavaType);
        model.put("pkAuto", meta.pkAuto);
        model.put("modelImports", buildModelImports(meta, true));
        model.put("dtoImports", buildModelImports(meta, false));
        model.put("convertorImports", buildConvertorImports(meta));
        return model;
    }

    /**
     * Model/DTO 导入块。jsonObject/jsonArray 的 javaObject 按全限定名 import，
     * 字段声明处使用短类型（见 DbMetaReader.shortType）。
     *
     * @param forDomainModel true=领域模型文件（与 javaObject 同包时不写重复 import）；false=DTO（web 包，需要 import）。
     */
    private String buildModelImports(TableMeta meta, boolean forDomainModel) {
        StringBuilder sb = new StringBuilder();
        boolean list = false;
        boolean map = false;
        String domainPackage = cfg.basePackage() + ".core.model.domain";
        for (ColumnMeta c : meta.columns) {
            if (c.enumColumn) {
                sb.append("import ").append(cfg.basePackage()).append(".core.model.enums.").append(c.enumClassName).append(";\n");
            }
            if (c.jsonElementType != null) {
                String importType = DbMetaReader.importableType(c.jsonElementType);
                if (importType != null && !(forDomainModel && importType.startsWith(domainPackage + "."))) {
                    sb.append("import ").append(importType).append(";\n");
                }
            }
            if (c.modelType != null && c.modelType.contains("List<")) {
                list = true;
            }
            if (c.modelType != null && c.modelType.contains("Map<")) {
                map = true;
            }
        }
        if (list) {
            sb.append("import java.util.List;\n");
        }
        if (map) {
            sb.append("import java.util.Map;\n");
        }
        return sb.toString();
    }

    private String buildConvertorImports(TableMeta meta) {
        StringBuilder sb = new StringBuilder();
        boolean convert = false;
        boolean objectUtil = false;
        boolean jsonUtil = false;
        boolean typeRef = false;
        for (ColumnMeta c : meta.columns) {
            if (c.enumColumn) {
                sb.append("import ").append(cfg.basePackage()).append(".core.model.enums.").append(c.enumClassName).append(";\n");
            }
            if ("ENUM".equals(c.conversion)) {
                objectUtil = true;
            }
            if ("COERCE".equals(c.conversion)) {
                convert = true;
            }
            if ("JSON_ARRAY".equals(c.conversion) || "JSON_OBJECT".equals(c.conversion)) {
                jsonUtil = true;
                if (c.toModelExpr != null && c.toModelExpr.contains("TypeReference")) {
                    typeRef = true;
                }
            }
        }
        if (convert) {
            sb.append("import cn.hutool.core.convert.Convert;\n");
        }
        if (objectUtil) {
            sb.append("import cn.hutool.core.util.ObjectUtil;\n");
        }
        if (jsonUtil) {
            sb.append("import ").append(cfg.basePackage()).append(".common.util.tools.JsonUtil;\n");
        }
        if (typeRef) {
            sb.append("import com.fasterxml.jackson.core.type.TypeReference;\n");
        }
        return sb.toString();
    }

    private boolean render(TableMeta meta, String templateName, String outputPattern,
                           Map<String, Object> model, boolean force) throws IOException, TemplateException {
        Template template = freemarker.getTemplate(templateName);
        StringWriter writer = new StringWriter();
        template.process(model, writer);

        String targetRel = outputPattern
                .replace("{pkg}", cfg.packagePath())
                .replace("{Class}", meta.className);
        Path target = cfg.outputDir.resolve(targetRel);
        if (Files.exists(target) && !force) {
            return false;
        }
        Files.createDirectories(target.getParent());
        Files.writeString(target, writer.toString(), StandardCharsets.UTF_8);
        return true;
    }

    private void printReport(int success, int skipped, int warned,
                             List<String> skipReasons, List<String> warnReasons) {
        System.out.println("[gen]");
        System.out.println("[gen] ┌─────────────────── 执行报告 ───────────────────┐");
        System.out.println("[gen] │ 成功: " + success + "    跳过: " + skipped + "    警告: " + warned + "                    │");
        System.out.println("[gen] ├────────────────────────────────────────────────┤");
        for (String reason : skipReasons) {
            System.out.println("[gen] │ [跳过] " + reason);
        }
        for (String reason : warnReasons) {
            System.out.println("[gen] │ [警告] " + reason);
        }
        System.out.println("[gen] └────────────────────────────────────────────────┘");
    }
}
