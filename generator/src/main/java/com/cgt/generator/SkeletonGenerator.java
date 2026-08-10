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

        int generated = 0;
        int skipped = 0;
        try (Stream<Path> walk = Files.walk(skeleton)) {
            for (Path source : walk.filter(Files::isRegularFile)
                    .filter(p -> !isSkipped(skeleton.relativize(p)))
                    .toList()) {
                Path relative = skeleton.relativize(source);
                String targetRel = replace(relative.toString().replace('\\', '/'));
                Path target = cfg.outputDir.resolve(targetRel);
                if (Files.exists(target)) {
                    skipped++;
                    continue;
                }
                Files.createDirectories(target.getParent());
                String content = replace(Files.readString(source, StandardCharsets.UTF_8));
                Files.writeString(target, content, StandardCharsets.UTF_8);
                generated++;
            }
        }
        System.out.println("[gen] 项目骨架初始化完成（生成 " + generated + " 个文件，跳过 " + skipped + " 个） -> " + cfg.outputDir);
    }

    /** 跳过构建产物/IDE 目录（target、.git、.idea、out）。 */
    private static boolean isSkipped(Path relative) {
        for (Path part : relative) {
            String name = part.toString();
            if ("target".equals(name) || ".git".equals(name) || ".idea".equals(name) || "out".equals(name)) {
                return true;
            }
        }
        return false;
    }

    private String replace(String text) {
        String out = text;
        out = out.replace(TOKENS.get(0)[0], cfg.projectPrefix + "Application");
        out = out.replace(TOKENS.get(1)[0], cfg.basePackage());
        out = out.replace(TOKENS.get(2)[0], cfg.toolPrefix);
        out = out.replace(TOKENS.get(3)[0], cfg.projectArtifactPrefix);
        out = out.replace(TOKENS.get(4)[0], cfg.groupId);
        return out;
    }
}
