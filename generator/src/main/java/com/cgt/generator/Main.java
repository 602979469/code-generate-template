package com.cgt.generator;

import java.util.HashMap;
import java.util.Map;

/**
 * 生成器入口。
 *
 * <pre>
 *   init   初始化新项目    -p AiProd -g com.jakt [-a aiprod] -o 输出目录
 *   table  按表生成 CRUD   -t sys_dept,member [-o 目标项目根目录] [-f 覆盖已存在文件]
 *   list   列出模板文件
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
            String cmd = args[0];
            // 防御：第一个参数必须是合法命令，避免 -c 被误当命令
            if (cmd.startsWith("-") || !(cmd.equals("init") || cmd.equals("table") || cmd.equals("list"))) {
                usage();
                return;
            }
            Map<String, String> opts = parseOptions(args, 1);
            GeneratorConfig config = GeneratorConfig.load(opts);

            switch (cmd) {
                case "init" -> {
                    config.validateNaming();
                    new SkeletonGenerator(config).run();
                }
                case "table" -> {
                    config.validateNaming();
                    new CrudGenerator(config, opts).run();
                }
                case "list" -> CrudGenerator.listTemplates(config);
                default -> usage();
            }
        } catch (Exception e) {
            System.err.println("[gen] 执行失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static Map<String, String> parseOptions(String[] args, int start) {
        Map<String, String> opts = new HashMap<>();
        for (int i = start; i < args.length; i++) {
            String key = args[i];
            if (!key.startsWith("-")) {
                throw new IllegalArgumentException("参数格式应为 -key value，非法参数: " + key);
            }
            String name = key.substring(1);
            if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                opts.put(name, args[++i]);
            } else {
                // 无值参数按布尔开关处理，如 -f
                opts.put(name, "true");
            }
        }
        return opts;
    }

    private static void usage() {
        System.out.println("""
                用法:
                  init:
                    ./gen.sh init -p <项目前缀> -g <groupId> -a <包名后缀> -tp <工具前缀> -o <输出目录>
                    示例: ./gen.sh init -p AiProd -g com.jakt -a aiprod -tp AiProd -o ../AiProd

                  table:
                    ./gen.sh table -t <表名1,表名2> -p <项目前缀> -g <groupId> -a <包名后缀> -tp <工具前缀> [-o <目标项目根目录>] [-f]
                    示例: ./gen.sh table -t sys_dept,member -p AiPlatform -g com.jakt -a aiplatform -tp AiPlatform -o .

                  list:
                    ./gen.sh list

                参数说明:
                  -p   项目前缀(驼峰,用于启动类名): 如 AiProd
                  -g   Maven groupId: 如 com.jakt
                  -a   artifactId/包名后缀(小写字母数字,Java 包名不允许连字符): 如 aiprod
                  -tp  工具类/异常/常量前缀(驼峰,由你自己指定,代码不做转换): 如 AiProd
                  -o   输出目录
                  -t   表名,多个用逗号分隔
                  -f   覆盖已存在文件(默认跳过)

                必填项:
                  init  : -p -g -a -tp -o 全部必填,无默认值
                  table : -t -p -g -a -tp 必填,-o 默认当前目录
                  list  : 无参数""");
    }
}
