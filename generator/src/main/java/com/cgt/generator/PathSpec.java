package com.cgt.generator;

import java.nio.file.Path;

/** 路径解析结果：文件根目录 + 包名。 */
public record PathSpec(Path root, String packageName) {
}
