package com.cgt.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * 项目初始化：复制 skeleton/ 并按 token 规则改名（AiPlatform -> AiProd 等）。
 */
public final class SkeletonGenerator {

    private static final List<String[]> TOKENS = List.of(
            new String[]{"AiplatformApplication", "APP"},
            new String[]{"com.jakt.aiplatform", "PKG"},
            new String[]{"AiPlatform", "PREFIX"},
            new String[]{"aiplatform", "ARTIFACT"},
            new String[]{"com.jakt", "GROUP"}
    );

    private final GeneratorConfig cfg;

    public SkeletonGenerator(GeneratorConfig cfg) {
        this.cfg = cfg;
    }

    public void run() throws IOException {
        Path skeleton = cfg.skeletonDir();
        if (!Files.isDirectory(skeleton)) {
            throw new IllegalStateException("skeleton 目录不存在: " + skeleton);
        }
        Files.createDirectories(cfg.outputDir);

        try (Stream<Path> walk = Files.walk(skeleton)) {
            for (Path source : walk.filter(Files::isRegularFile).toList()) {
                Path relative = skeleton.relativize(source);
                String targetRel = replace(relative.toString().replace('\\', '/'));
                Path target = cfg.outputDir.resolve(targetRel);
                Files.createDirectories(target.getParent());
                String content = replace(Files.readString(source, StandardCharsets.UTF_8));
                Files.writeString(target, content, StandardCharsets.UTF_8);
                System.out.println("[gen] 生成 " + targetRel);
            }
        }
        System.out.println("[gen] 项目初始化完成 -> " + cfg.outputDir);
    }

    private String replace(String text) {
        String out = text;
        out = out.replace(TOKENS.get(0)[0], cfg.projectPrefix + "Application");
        out = out.replace(TOKENS.get(1)[0], cfg.basePackage());
        out = out.replace(TOKENS.get(2)[0], cfg.projectPrefix);
        out = out.replace(TOKENS.get(3)[0], cfg.projectArtifactPrefix);
        out = out.replace(TOKENS.get(4)[0], cfg.groupId);
        return out;
    }
}
