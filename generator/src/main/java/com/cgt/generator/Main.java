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
                  init   初始化新项目   -p AiProd -g com.jakt [-a aiprod] -o 输出目录
                  table  按表生成 CRUD  -t sys_dept,member [-o 目标项目根目录] [-f]
                  list   列出表级模板
                全局参数:
                  -c 配置文件路径(默认 generator.properties)""");
    }
}
