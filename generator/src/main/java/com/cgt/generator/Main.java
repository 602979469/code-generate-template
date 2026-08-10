package com.cgt.generator;

/**
 * 生成器入口：只有一个功能 —— 按配置文件生成。
 *
 * <pre>
 *   ./gen.sh &lt;配置文件&gt;   初始化项目(跳过已存在) + 按 tables 生成表级 CRUD
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
            GeneratorConfig config = GeneratorConfig.load(args[0]);
            config.validateNaming();

            // 1. 初始化项目骨架(已存在文件跳过,不覆盖)
            new SkeletonGenerator(config).run();
            // 2. generateExample：后台建表 + 示例 POJO + 注入内置示例表配置
            if (config.generateExample) {
                for (GeneratorConfig.TableConfig table : config.tables) {
                    if (ExampleGenerator.DEMO_TABLE.equalsIgnoreCase(table.dbTableName)) {
                        throw new IllegalArgumentException("tables 中重复配置了 example 表：示例表由 generateExample 内置生成"
                                + "（自动建表），请从 tables 中移除 db_table_name: example");
                    }
                }
                ExampleGenerator.createTable(config);
                ExampleGenerator.generatePojos(config);
                config.tables.add(0, ExampleGenerator.exampleTableConfig(config.basePackage()));
            }
            // 3. 按 tables 生成表级 CRUD(每张表独立判定 成功/跳过/强制覆盖)
            new CrudGenerator(config).run();
        } catch (Exception e) {
            System.err.println("[gen] 执行失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void usage() {
        System.out.println("""
                用法:
                  ./gen.sh <配置文件>      按配置文件初始化项目并生成表级 CRUD

                说明:
                  所有配置(项目命名/数据库/tables)都通过 YAML 配置文件提供,不再使用命令行参数。
                  未提供配置文件时,直接运行 ./gen.sh 可交互生成配置模板(generate.yaml)。""");
    }
}
