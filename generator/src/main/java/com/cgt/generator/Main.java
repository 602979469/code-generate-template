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
                case "init" -> new SkeletonGenerator(config).run();
                case "table" -> new CrudGenerator(config, opts).run();
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
                  init   初始化新项目   -p 项目前缀 -g groupId [-a artifact后缀] -o 输出目录
                  table  按表生成 CRUD  -t 表名1,表名2 [-p 项目前缀] [-g groupId] [-a artifact后缀] [-o 目标项目] [-f]
                  list   列出表级模板

                示例:
                  init  -p AiProd -g com.jakt -a aiprod -o ../AiProd
                  table -t sys_dept -o /Users/jakt/IdeaProjects/aiplatform

                说明:
                  -p/-g/-a 指定项目命名;不传时 init 用 generator.properties 默认值,
                  table 会自动从目标项目识别项目名(识别失败才用默认值)。
                全局参数:
                  -c 配置文件路径(默认 generator.properties)""");
    }
}
