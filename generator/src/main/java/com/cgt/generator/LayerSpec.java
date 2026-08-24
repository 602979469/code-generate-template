package com.cgt.generator;

import java.util.List;

/**
 * 基线层目录表条目（统一基线，任何布局不变）。
 *
 * @param id                   层 id
 * @param mavenModuleDir       1.0 层 Maven 模块目录（flat / 未分配模块时的落点），如 "core/model"
 * @param packagePath          层包名，如 "core.model"
 * @param sub                  子包路径（可含 /），如 "repository/impl"
 * @param fileRules            文件名规则（{Model} 替换为模型名），如 ["{Model}DO.java"]
 * @param common               共享层（common.*，不挂业务模块）
 * @param skipWhenNoController generateController=false 时跳过（web / biz 层）
 * @param resource             资源文件（src/main/resources，非 java 包）
 */
public record LayerSpec(
        String id,
        String mavenModuleDir,
        String packagePath,
        String sub,
        List<String> fileRules,
        boolean common,
        boolean skipWhenNoController,
        boolean resource) {

    public String packageSub() {
        return sub.replace('/', '.');
    }
}
