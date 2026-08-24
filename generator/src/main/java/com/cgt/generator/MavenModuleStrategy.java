package com.cgt.generator;

import java.nio.file.Path;

/** maven-module：业务模块拆 Maven 子项目，平铺在根目录；包路径与 aggregated 一致（带 modules 前缀）。 */
public final class MavenModuleStrategy implements LayoutStrategy {

    private final String artifactPrefix;

    public MavenModuleStrategy(String artifactPrefix) {
        this.artifactPrefix = artifactPrefix;
    }

    @Override
    public String id() {
        return "maven-module";
    }

    @Override
    public String packagePrefix() {
        return "modules";
    }

    @Override
    public boolean businessInLayer() {
        return false;
    }

    @Override
    public Path moduleRoot(Path outputDir, String module) {
        return outputDir.resolve(artifactPrefix + "-" + module);
    }
}
