package com.cgt.generator;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 生成器入口：只有一个功能 —— 按配置文件生成。
 *
 * <pre>
 *   ./gen.sh &lt;配置文件&gt;                初始化项目(跳过已存在) + 按 tables 生成表级 CRUD
 *   ./gen.sh --validate &lt;配置文件&gt;    校验模式：只检查配置/表结构/生成计划，不落盘
 * </pre>
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                usage();
                return;
            }
            boolean validate = false;
            String configPath;
            if ("--validate".equals(args[0]) || "--dry-run".equals(args[0])) {
                validate = true;
                if (args.length < 2) {
                    System.out.println("[gen] 校验模式缺少配置文件：--validate <配置文件>");
                    usage();
                    return;
                }
                configPath = args[1];
            } else {
                configPath = args[0];
            }

            GeneratorConfig config = GeneratorConfig.load(configPath);
            config.validateNaming();

            if (validate) {
                runValidate(config);
            } else {
                // 1. 初始化项目骨架(已存在文件跳过,不覆盖)
                new SkeletonGenerator(config).run();
                // 2. 按 tables 生成表级 CRUD(每张表独立判定 成功/跳过/强制覆盖)
                new CrudGenerator(config).run();
            }
        } catch (Exception e) {
            System.err.println("[gen] 执行失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 校验模式：加载配置 + 连接数据库读表结构，逐表输出生成计划（文件数/枚举/跳过或覆盖），不写任何文件。
     */
    private static void runValidate(GeneratorConfig cfg) throws Exception {
        System.out.println("[gen] 校验模式（--validate）：只检查配置与表结构，不生成任何文件");
        System.out.println("[gen] 项目: " + cfg.projectPrefix + " / " + cfg.basePackage()
                + "，输出目录: " + cfg.outputDir);
        if (cfg.tables.isEmpty()) {
            System.out.println("[gen] 未配置 tables，仅校验项目命名");
            return;
        }

        int ok = 0;
        int fail = 0;
        for (GeneratorConfig.TableConfig table : cfg.tables) {
            TableMeta meta;
            try {
                meta = DbMetaReader.read(cfg, table);
            } catch (Exception e) {
                System.out.println("[gen] ✗ " + table.dbTableName + ": " + e.getMessage());
                fail++;
                continue;
            }

            StringBuilder enums = new StringBuilder();
            for (ColumnMeta c : meta.columns) {
                if (c.enumColumn) {
                    enums.append(enums.isEmpty() ? "" : ", ").append(c.columnName).append("->").append(c.enumClassName);
                }
            }

            String state = "将生成";
            Path doPath = cfg.outputDir.resolve("common/dal/src/main/java/" + cfg.packagePath()
                    + "/common/dal/dataobject/" + meta.className + "DO.java");
            boolean doExists = Files.exists(doPath);
            String existingEnum = null;
            for (ColumnMeta c : meta.columns) {
                if (!c.enumColumn) {
                    continue;
                }
                Path p = cfg.outputDir.resolve("core/model/src/main/java/" + cfg.packagePath()
                        + "/core/model/enums/" + c.enumClassName + ".java");
                if (Files.exists(p)) {
                    existingEnum = c.enumClassName;
                    break;
                }
            }
            if (doExists && !table.forceCreate) {
                state = "将跳过（DO 已存在，不覆盖）";
            } else if (existingEnum != null && !table.forceCreate) {
                state = "将跳过（枚举 " + existingEnum + " 已存在，不覆盖）";
            } else if (table.forceCreate && (doExists || existingEnum != null)) {
                state = "将覆盖（force_create：" + (existingEnum != null ? "枚举 " + existingEnum : "DO")
                        + " 已存在，请注意会覆盖手动修改的代码）";
            }

            String mode = meta.compositePk ? "复合主键（ByKey 全键 CRUD）" : "单主键";
            String timeMode = meta.createTimeAuto && meta.updateTimeAuto
                    ? "时间列由 DB 自动维护"
                    : "时间列未自动维护，INSERT/UPDATE 用 NOW()";
            System.out.println("[gen] " + (meta.compositePk ? "◆ " : "· ") + table.dbTableName
                    + " -> " + meta.className + " [" + mode + "]");
            System.out.println("[gen]    主键: " + meta.pkColumns.stream()
                    .map(c -> c.columnName + "(" + c.javaType + ")")
                    .collect(java.util.stream.Collectors.joining(", "))
                    + "，逻辑删除: " + meta.logicDeleteEnabled + "，时间列: " + timeMode);
            System.out.println("[gen]    计划: " + state + "，代码文件 "
                    + CrudGenerator.plannedFileCount(meta, table.generateController)
                    + " 个 + 1 份 DDL"
                    + (enums.isEmpty() ? "" : "，枚举: {" + enums + "}"));
            ok++;
        }
        System.out.println("[gen] 校验完成：通过 " + ok + " 张表" + (fail > 0 ? "，失败 " + fail + " 张表" : "")
                + "（未生成任何文件）");
        if (fail > 0) {
            System.exit(1);
        }
    }

    private static void usage() {
        System.out.println("""
                用法:
                  ./gen.sh <配置文件>               按配置文件初始化项目并生成表级 CRUD
                  ./gen.sh --validate <配置文件>    校验模式：只检查配置/表结构/生成计划，不落盘

                说明:
                  所有配置(项目命名/数据库/tables)都通过 YAML 配置文件提供,不再使用命令行参数。
                  未提供配置文件时,直接运行 ./gen.sh 可交互生成配置模板(generate.yaml)。
                  校验模式示例: ./gen.sh --validate ./generate.yaml""");
    }
}
