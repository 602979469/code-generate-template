package com.cgt.generator;

import java.nio.file.Path;

/** flat（平铺式）：不新建 Maven 模块，业务模块名前置（{basePackage}.{module}.{层包}.{子包}）。 */
public final class FlatStrategy implements LayoutStrategy {

    @Override
    public String id() {
        return "flat";
    }

    @Override
    public String packagePrefix() {
        return "";
    }

    @Override
    public boolean businessInLayer() {
        return true;
    }

    @Override
    public Path moduleRoot(Path outputDir, String module) {
        return null;
    }
}
