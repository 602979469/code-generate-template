package com.cgt.generator;

import java.nio.file.Path;

/** aggregated（聚合式）：业务模块拆 Maven 子项目，收在 modules/ 目录下。 */
public final class AggregatedStrategy implements LayoutStrategy {

    private final String artifactPrefix;

    public AggregatedStrategy(String artifactPrefix) {
        this.artifactPrefix = artifactPrefix;
    }

    @Override
    public String id() {
        return "aggregated";
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
        return outputDir.resolve("modules").resolve(artifactPrefix + "-" + module);
    }
}
