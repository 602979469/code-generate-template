package com.cgt.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 目录结构生成（验证用）：按布局策略生成完整的目录骨架，每个文件用空文件占位，不写任何真实内容。
 *
 * <p>用于确认三种布局（flat / aggregated / maven-module）的目录结构是否正确。
 */
public final class StructureGenerator {

    private final GeneratorConfig cfg;

    public StructureGenerator(GeneratorConfig cfg) {
        this.cfg = cfg;
    }

    public void run() throws IOException {
        LayoutStrategy strategy = cfg.layoutStrategy();
        PathResolver resolver = new PathResolver(cfg);
        List<Path> created = new ArrayList<>();

        // 1. 根 pom 占位
        created.add(touch(cfg.outputDir.resolve("pom.xml")));

        // 2. common 模块与 bootstrap 占位
        for (String dir : List.of("common/dal", "common/util", "common/framework", "common/integration", "bootstrap")) {
            created.add(touch(cfg.outputDir.resolve(dir).resolve("pom.xml")));
        }

        // 3. 业务模块根（aggregated / maven-module）：模块根 + pom 占位
        if (!strategy.businessInLayer()) {
            for (GeneratorConfig.ModuleConfig m : cfg.modules) {
                created.add(touch(strategy.moduleRoot(cfg.outputDir, m.name).resolve("pom.xml")));
            }
        }
        warnUnknownModules();

        // 4. 每张表：各层空文件占位
        for (GeneratorConfig.TableConfig t : cfg.tables) {
            for (LayerSpec layer : LayerCatalog.ALL) {
                if (layer.skipWhenNoController() && !t.generateController) {
                    continue;
                }
                if (layer.resource()) {
                    Path dir = resolver.resourceDir(layer);
                    for (String rule : layer.fileRules()) {
                        created.add(touch(dir.resolve(rule.replace("{Model}", t.modelName))));
                    }
                } else {
                    PathSpec spec = resolver.resolve(layer, t.module);
                    for (String rule : layer.fileRules()) {
                        created.add(touch(spec.root().resolve(rule.replace("{Model}", t.modelName))));
                    }
                }
            }
        }

        // 5. bootstrap Application 占位
        Path appDir = cfg.outputDir.resolve("bootstrap").resolve("src/main/java")
                .resolve(cfg.packagePath().replace('.', '/')).resolve("bootstrap");
        created.add(touch(appDir.resolve(cfg.projectPrefix + "Application.java")));

        printSummary(created);
    }

    private void warnUnknownModules() {
        for (GeneratorConfig.TableConfig t : cfg.tables) {
            if (t.module != null && !t.module.isBlank()
                    && cfg.modules.stream().noneMatch(m -> m.name.equals(t.module))) {
                System.out.println("[gen] 警告: 表 " + t.dbTableName + " 的 module=" + t.module
                        + " 未在 modules 列表中声明，按未分配模块处理");
            }
        }
    }

    private Path touch(Path file) throws IOException {
        Files.createDirectories(file.getParent());
        if (!Files.exists(file)) {
            Files.createFile(file);
        }
        return file;
    }

    private void printSummary(List<Path> created) {
        System.out.println("[gen] 目录结构生成完成（" + created.size() + " 个文件） -> " + cfg.outputDir);
        created.stream()
                .map(p -> cfg.outputDir.relativize(p).toString().replace('\\', '/'))
                .sorted(Comparator.naturalOrder())
                .forEach(p -> System.out.println("    " + p));
        System.out.println("[gen] ⚠️ 占位文件均为 0 字节（仅用于查看目录结构）；正式生成前请删除输出目录内容，"
                + "否则已存在文件会被跳过（除非 force_create: true）");
    }
}
