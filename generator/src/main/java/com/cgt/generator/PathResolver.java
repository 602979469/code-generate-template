package com.cgt.generator;

import java.nio.file.Path;

/**
 * 统一口径：resolve(层, 业务模块) → {文件根目录, 包名}。
 *
 * <p>公式（见《02-代码风格与模块设计》）：
 * <pre>
 * 共享层    = {basePackage}.{层包}.{子包}
 * flat      = {basePackage}.{业务模块}.{层包}.{子包}
 * aggregated= {basePackage}.modules.{业务模块}.{层包}.{子包}
 * maven-module = 同 aggregated（仅根目录不同）
 * </pre>
 */
public final class PathResolver {

    private final GeneratorConfig cfg;
    private final LayoutStrategy strategy;

    public PathResolver(GeneratorConfig cfg) {
        this.cfg = cfg;
        this.strategy = cfg.layoutStrategy();
    }

    /** 解析某层在给定业务模块下的落点（java 包文件）。 */
    public PathSpec resolve(LayerSpec layer, String module) {
        String base = cfg.basePackage();
        String sub = layer.packageSub();
        String pkg;
        Path root;

        if (layer.common() || isBlank(module)) {
            pkg = base + "." + layer.packagePath() + "." + sub;
            root = cfg.outputDir.resolve(layer.mavenModuleDir());
        } else if (strategy.businessInLayer()) {
            // flat：业务模块名前置（{basePackage}.{module}.{层包}.{子包}）
            pkg = base + "." + module + "." + layer.packagePath() + "." + sub;
            root = cfg.outputDir.resolve(layer.mavenModuleDir());
        } else {
            // aggregated / maven-module：包路径一致（带 modules 前缀）；
            // 业务模块内再按层组拆 Maven 子模块（{artifact}-{module}-{web|core|biz}），根目录由策略 + 层组决定
            String prefix = strategy.packagePrefix();
            pkg = base + (prefix.isEmpty() ? "" : "." + prefix) + "." + module + "." + layer.packagePath() + "." + sub;
            root = strategy.moduleRoot(cfg.outputDir, module);
            if (layer.group() != null) {
                root = root.resolve(cfg.projectArtifactPrefix + "-" + module + "-" + layer.group());
            }
        }
        return new PathSpec(root.resolve("src/main/java").resolve(pkg.replace('.', '/')), pkg);
    }

    /** 资源文件目录（Mapper.xml 等）。 */
    public Path resourceDir(LayerSpec layer) {
        return cfg.outputDir.resolve(layer.mavenModuleDir()).resolve("src/main/resources").resolve(layer.sub());
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
