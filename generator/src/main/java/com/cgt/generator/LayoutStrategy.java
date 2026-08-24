package com.cgt.generator;

import java.nio.file.Path;

/**
 * 布局策略（数据驱动）：只提供布局参数，不含任何层/文件判断。
 *
 * <p>三种模式：
 * <ul>
 *   <li>flat：不新建 Maven 模块，业务模块作为层内子包；</li>
 *   <li>aggregated：业务模块拆 Maven 子项目，收在 modules/ 目录下，包路径带 modules 前缀；</li>
 *   <li>maven-module：业务模块拆 Maven 子项目平铺在根下，包路径带 modules 前缀。</li>
 * </ul>
 */
public interface LayoutStrategy {

    /** 策略 id："flat" | "aggregated" | "maven-module"。 */
    String id();

    /** 包路径前缀：""（flat）或 "modules"（aggregated / maven-module）。 */
    String packagePrefix();

    /** 业务模块是否作为层内子包（flat=true；aggregated / maven-module=false）。 */
    boolean businessInLayer();

    /**
     * 业务模块根目录。
     *
     * <p>aggregated / maven-module 使用；flat 返回 null（业务层仍落在基线层模块目录内）。
     */
    Path moduleRoot(Path outputDir, String module);
}
