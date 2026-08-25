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
            new String[]{"com/jakt/aiplatform", "PKG_PATH"},
            new String[]{"AiPlatform", "PREFIX"},
            new String[]{"aiplatform", "ARTIFACT"},
            new String[]{"com.jakt", "GROUP"},
            new String[]{"com/jakt", "GROUP_PATH"}
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

        // aggregated / maven-module：业务模块拆独立 Maven 子项目，骨架不再复制 flat 的 biz/core 空层模块与根/web/bootstrap pom
        boolean layoutFlat = cfg.layoutStrategy().businessInLayer();
        int generated = 0;
        int skipped = 0;
        try (Stream<Path> walk = Files.walk(skeleton)) {
            for (Path source : walk.filter(Files::isRegularFile)
                    .filter(p -> !isSkipped(skeleton.relativize(p)))
                    .filter(p -> layoutFlat || !isSkippedForLayout(skeleton.relativize(p)))
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
        if (!layoutFlat) {
            generated += generateLayoutPoms();
        }
        System.out.println("[gen] 项目骨架初始化完成（生成 " + generated + " 个文件，跳过 " + skipped + " 个） -> " + cfg.outputDir);
    }

    /** 跳过构建产物/IDE 目录与系统垃圾文件（target、.git、.idea、out、.DS_Store 等）。 */
    private static boolean isSkipped(Path relative) {
        for (Path part : relative) {
            String name = part.toString();
            if ("target".equals(name) || ".git".equals(name) || ".idea".equals(name) || "out".equals(name)
                    || ".DS_Store".equals(name) || "Thumbs.db".equals(name) || "desktop.ini".equals(name)) {
                return true;
            }
        }
        return false;
    }

    /** 非 flat 布局时跳过的骨架文件：flat 空层模块（biz/core）与需要按布局重写的 pom。 */
    private static boolean isSkippedForLayout(Path relative) {
        String first = relative.getName(0).toString();
        if ("biz".equals(first) || "core".equals(first)) {
            return true;
        }
        String path = relative.toString().replace('\\', '/');
        return "pom.xml".equals(path) || "web/pom.xml".equals(path) || "bootstrap/pom.xml".equals(path);
    }

    /** 非 flat 布局：生成根 pom、web 共享模块 pom、bootstrap pom、业务模块聚合 pom 与层子模块 pom。 */
    private int generateLayoutPoms() throws IOException {
        int count = 0;
        count += writePom(cfg.outputDir.resolve("pom.xml"), buildRootPom());
        count += writePom(cfg.outputDir.resolve("web/pom.xml"), buildWebPom());
        count += writePom(cfg.outputDir.resolve("bootstrap/pom.xml"), buildBootstrapPom());
        for (GeneratorConfig.ModuleConfig m : cfg.modules) {
            Path moduleRoot = cfg.layoutStrategy().moduleRoot(cfg.outputDir, m.name);
            count += writePom(moduleRoot.resolve("pom.xml"), buildModulePom(m.name));
            for (String group : List.of("core", "biz", "web")) {
                Path groupPom = moduleRoot.resolve(cfg.projectArtifactPrefix + "-" + m.name + "-" + group)
                        .resolve("pom.xml");
                count += writePom(groupPom, buildLayerModulePom(m.name, group));
            }
        }
        return count;
    }

    private int writePom(Path pom, String content) throws IOException {
        if (Files.exists(pom)) {
            return 0;
        }
        Files.createDirectories(pom.getParent());
        Files.writeString(pom, content, StandardCharsets.UTF_8);
        return 1;
    }

    private String pomHeader() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                """;
    }

    /** 业务模块 pom 到根 pom 的相对路径（aggregated 在 modules/ 下，maven-module 平铺在根下）。 */
    private String rootRelativePath() {
        return cfg.layoutStrategy().businessInLayer() ? "" : (isAggregated() ? "../.." : "..");
    }

    private boolean isAggregated() {
        return "aggregated".equals(cfg.moduleLayout);
    }

    private String moduleElement(String module) {
        return isAggregated() ? "modules/" + cfg.projectArtifactPrefix + "-" + module
                : cfg.projectArtifactPrefix + "-" + module;
    }

    private String buildRootPom() {
        StringBuilder modules = new StringBuilder();
        for (GeneratorConfig.ModuleConfig m : cfg.modules) {
            modules.append("\n        <module>").append(moduleElement(m.name)).append("</module>");
        }
        StringBuilder bizArtifacts = new StringBuilder();
        for (GeneratorConfig.ModuleConfig m : cfg.modules) {
            for (String artifact : moduleArtifacts(m.name)) {
                bizArtifacts.append("""
                            <dependency>
                                <groupId>%s</groupId>
                                <artifactId>%s</artifactId>
                                <version>${project.version}</version>
                            </dependency>
                        """.formatted(cfg.groupId, artifact));
            }
        }
        return pomHeader() + """
                    <groupId>%s</groupId>
                    <artifactId>%s</artifactId>
                    <version>0.1.0-SNAPSHOT</version>
                    <packaging>pom</packaging>

                    <name>%s</name>
                    <description>%s：业务模块独立 Maven 子项目（%s）</description>

                    <modules>
                        <module>common</module>
                        <module>web</module>
                        <module>bootstrap</module>%s
                    </modules>

                    <properties>
                        <java.version>17</java.version>
                        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
                        <spring-boot.version>4.0.6</spring-boot.version>
                        <mybatis-spring-boot.version>4.0.1</mybatis-spring-boot.version>
                        <springdoc.version>3.0.3</springdoc.version>
                        <hutool.version>5.8.18</hutool.version>
                        <sa-token.version>1.45.0</sa-token.version>
                        <maven-compiler-plugin.version>3.11.0</maven-compiler-plugin.version>
                        <lombok.version>1.18.46</lombok.version>
                    </properties>

                    <dependencyManagement>
                        <dependencies>
                            <dependency>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-dependencies</artifactId>
                                <version>${spring-boot.version}</version>
                                <type>pom</type>
                                <scope>import</scope>
                            </dependency>
                            <dependency>
                                <groupId>org.mybatis.spring.boot</groupId>
                                <artifactId>mybatis-spring-boot-starter</artifactId>
                                <version>${mybatis-spring-boot.version}</version>
                            </dependency>
                            <dependency>
                                <groupId>org.springdoc</groupId>
                                <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
                                <version>${springdoc.version}</version>
                            </dependency>
                            <dependency>
                                <groupId>cn.hutool</groupId>
                                <artifactId>hutool-all</artifactId>
                                <version>${hutool.version}</version>
                            </dependency>
                            <dependency>
                                <groupId>cn.dev33</groupId>
                                <artifactId>sa-token-core</artifactId>
                                <version>${sa-token.version}</version>
                            </dependency>
                            <dependency>
                                <groupId>cn.dev33</groupId>
                                <artifactId>sa-token-spring-boot4-starter</artifactId>
                                <version>${sa-token.version}</version>
                            </dependency>
                            <dependency>
                                <groupId>cn.dev33</groupId>
                                <artifactId>sa-token-redis-template</artifactId>
                                <version>${sa-token.version}</version>
                            </dependency>

                            <!-- 本工程模块 -->
                            <dependency>
                                <groupId>%s</groupId>
                                <artifactId>%s-common-util</artifactId>
                                <version>${project.version}</version>
                            </dependency>
                            <dependency>
                                <groupId>%s</groupId>
                                <artifactId>%s-common-framework</artifactId>
                                <version>${project.version}</version>
                            </dependency>
                            <dependency>
                                <groupId>%s</groupId>
                                <artifactId>%s-common-dal</artifactId>
                                <version>${project.version}</version>
                            </dependency>
                            <dependency>
                                <groupId>%s</groupId>
                                <artifactId>%s-common-integration</artifactId>
                                <version>${project.version}</version>
                            </dependency>
                            <dependency>
                                <groupId>%s</groupId>
                                <artifactId>%s-web</artifactId>
                                <version>${project.version}</version>
                            </dependency>
                            <dependency>
                                <groupId>%s</groupId>
                                <artifactId>%s-bootstrap</artifactId>
                                <version>${project.version}</version>
                            </dependency>
                %s
                        </dependencies>
                    </dependencyManagement>

                    <!-- Lombok 统一在此引入（provided，仅编译期生效） -->
                    <dependencies>
                        <dependency>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <scope>provided</scope>
                        </dependency>
                    </dependencies>

                    <build>
                        <pluginManagement>
                            <plugins>
                                <plugin>
                                    <groupId>org.apache.maven.plugins</groupId>
                                    <artifactId>maven-compiler-plugin</artifactId>
                                    <version>${maven-compiler-plugin.version}</version>
                                    <configuration>
                                        <release>${java.version}</release>
                                        <encoding>${project.build.sourceEncoding}</encoding>
                                        <!-- 保留参数名，供 Spring MVC/MyBatis 反射使用 -->
                                        <parameters>true</parameters>
                                        <!-- JDK 23+ 不再自动执行 classpath 上的注解处理器，显式声明 Lombok -->
                                        <annotationProcessorPaths>
                                            <path>
                                                <groupId>org.projectlombok</groupId>
                                                <artifactId>lombok</artifactId>
                                                <version>${lombok.version}</version>
                                            </path>
                                        </annotationProcessorPaths>
                                    </configuration>
                                </plugin>
                            </plugins>
                        </pluginManagement>
                    </build>
                </project>
                """.formatted(cfg.groupId, cfg.projectArtifactPrefix, cfg.projectPrefix, cfg.projectPrefix,
                cfg.moduleLayout, modules, cfg.groupId, cfg.projectArtifactPrefix,
                cfg.groupId, cfg.projectArtifactPrefix, cfg.groupId, cfg.projectArtifactPrefix,
                cfg.groupId, cfg.projectArtifactPrefix, cfg.groupId, cfg.projectArtifactPrefix,
                cfg.groupId, cfg.projectArtifactPrefix, bizArtifacts);
    }

    private String buildWebPom() {
        return pomHeader() + """
                    <parent>
                        <groupId>%s</groupId>
                        <artifactId>%s</artifactId>
                        <version>0.1.0-SNAPSHOT</version>
                        <relativePath>../pom.xml</relativePath>
                    </parent>

                    <artifactId>%s-web</artifactId>
                    <name>%s-web</name>
                    <description>web 共享层：ApiTemplate/ApiResult、ParamChecker 基类、全局异常处理、日志过滤（业务 Controller 在各自业务模块内）</description>

                    <dependencies>
                        <dependency>
                            <groupId>%s</groupId>
                            <artifactId>%s-common-util</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>%s</groupId>
                            <artifactId>%s-common-framework</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>cn.dev33</groupId>
                            <artifactId>sa-token-spring-boot4-starter</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-web</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-validation</artifactId>
                        </dependency>
                        <!-- ApiTemplate / 全局异常处理器用到 DataIntegrityViolationException（spring-tx） -->
                        <dependency>
                            <groupId>org.springframework</groupId>
                            <artifactId>spring-tx</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.springdoc</groupId>
                            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-test</artifactId>
                            <scope>test</scope>
                        </dependency>
                    </dependencies>
                </project>
                """.formatted(cfg.groupId, cfg.projectArtifactPrefix, cfg.projectArtifactPrefix,
                cfg.projectArtifactPrefix, cfg.groupId, cfg.projectArtifactPrefix,
                cfg.groupId, cfg.projectArtifactPrefix);
    }

    private String buildBootstrapPom() {
        StringBuilder bizDeps = new StringBuilder();
        for (GeneratorConfig.ModuleConfig m : cfg.modules) {
            bizDeps.append("""
                        <dependency>
                            <groupId>%s</groupId>
                            <artifactId>%s-%s-web</artifactId>
                        </dependency>
                    """.formatted(cfg.groupId, cfg.projectArtifactPrefix, m.name));
        }
        return pomHeader() + """
                    <parent>
                        <groupId>%s</groupId>
                        <artifactId>%s</artifactId>
                        <version>0.1.0-SNAPSHOT</version>
                        <relativePath>../pom.xml</relativePath>
                    </parent>

                    <artifactId>%s-bootstrap</artifactId>
                    <name>%s-bootstrap</name>
                    <description>启动模块：MainApplication、注解扫描、配置文件（唯一可启动模块）</description>

                    <dependencies>
                        <dependency>
                            <groupId>%s</groupId>
                            <artifactId>%s-web</artifactId>
                        </dependency>
                %s
                        <dependency>
                            <groupId>%s</groupId>
                            <artifactId>%s-common-dal</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>%s</groupId>
                            <artifactId>%s-common-util</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>%s</groupId>
                            <artifactId>%s-common-integration</artifactId>
                        </dependency>

                        <dependency>
                            <groupId>com.mysql</groupId>
                            <artifactId>mysql-connector-j</artifactId>
                            <scope>runtime</scope>
                        </dependency>
                        <dependency>
                            <groupId>cn.dev33</groupId>
                            <artifactId>sa-token-spring-boot4-starter</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>cn.dev33</groupId>
                            <artifactId>sa-token-redis-template</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-data-redis</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.apache.commons</groupId>
                            <artifactId>commons-pool2</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-test</artifactId>
                            <scope>test</scope>
                        </dependency>
                    </dependencies>

                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-maven-plugin</artifactId>
                                <version>${spring-boot.version}</version>
                                <configuration>
                                    <mainClass>%s.bootstrap.%sApplication</mainClass>
                                </configuration>
                                <executions>
                                    <execution>
                                        <goals>
                                            <goal>repackage</goal>
                                        </goals>
                                    </execution>
                                </executions>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """.formatted(cfg.groupId, cfg.projectArtifactPrefix, cfg.projectArtifactPrefix,
                cfg.projectArtifactPrefix, cfg.groupId, cfg.projectArtifactPrefix, bizDeps,
                cfg.groupId, cfg.projectArtifactPrefix, cfg.groupId, cfg.projectArtifactPrefix,
                cfg.groupId, cfg.projectArtifactPrefix, cfg.basePackage(), cfg.projectPrefix);
    }

    private String buildModulePom(String module) {
        return pomHeader() + """
                    <parent>
                        <groupId>%s</groupId>
                        <artifactId>%s</artifactId>
                        <version>0.1.0-SNAPSHOT</version>
                        <relativePath>%s</relativePath>
                    </parent>

                    <artifactId>%s-%s</artifactId>
                    <packaging>pom</packaging>
                    <name>%s-%s</name>
                    <description>业务模块 %s 聚合：web / core / biz 三个 Maven 子模块</description>

                    <modules>
                        <module>%s-%s-core</module>
                        <module>%s-%s-biz</module>
                        <module>%s-%s-web</module>
                    </modules>
                </project>
                """.formatted(cfg.groupId, cfg.projectArtifactPrefix, rootRelativePath(),
                cfg.projectArtifactPrefix, module, cfg.projectArtifactPrefix, module, module,
                cfg.projectArtifactPrefix, module, cfg.projectArtifactPrefix, module,
                cfg.projectArtifactPrefix, module);
    }

    /** 业务模块的 Maven 构件清单（聚合器 + web/core/biz 三个层子模块），供根 pom dependencyManagement 登记。 */
    private List<String> moduleArtifacts(String module) {
        return List.of(
                cfg.projectArtifactPrefix + "-" + module,
                cfg.projectArtifactPrefix + "-" + module + "-core",
                cfg.projectArtifactPrefix + "-" + module + "-biz",
                cfg.projectArtifactPrefix + "-" + module + "-web");
    }

    /** 业务模块内的层子模块 pom（core / biz / web）。 */
    private String buildLayerModulePom(String module, String group) {
        String deps = switch (group) {
            case "core" -> """
                            <dependency>
                                <groupId>%s</groupId>
                                <artifactId>%s-common-util</artifactId>
                            </dependency>
                            <dependency>
                                <groupId>%s</groupId>
                                <artifactId>%s-common-framework</artifactId>
                            </dependency>
                            <dependency>
                                <groupId>%s</groupId>
                                <artifactId>%s-common-dal</artifactId>
                            </dependency>
                            <dependency>
                                <groupId>cn.hutool</groupId>
                                <artifactId>hutool-all</artifactId>
                            </dependency>
                            <dependency>
                                <groupId>org.springframework</groupId>
                                <artifactId>spring-context</artifactId>
                            </dependency>
                            <!-- Jackson 注解：生成枚举的 @JsonFormat(OBJECT) / @JsonCreator（provided） -->
                            <dependency>
                                <groupId>com.fasterxml.jackson.core</groupId>
                                <artifactId>jackson-annotations</artifactId>
                                <scope>provided</scope>
                            </dependency>
                        """.formatted(cfg.groupId, cfg.projectArtifactPrefix,
                    cfg.groupId, cfg.projectArtifactPrefix, cfg.groupId, cfg.projectArtifactPrefix);
            case "biz" -> """
                            <dependency>
                                <groupId>%s</groupId>
                                <artifactId>%s-common-util</artifactId>
                            </dependency>
                            <dependency>
                                <groupId>%s</groupId>
                                <artifactId>%s-common-framework</artifactId>
                            </dependency>
                            <dependency>
                                <groupId>%s</groupId>
                                <artifactId>%s-%s-core</artifactId>
                            </dependency>
                            <dependency>
                                <groupId>org.springframework</groupId>
                                <artifactId>spring-context</artifactId>
                            </dependency>
                        """.formatted(cfg.groupId, cfg.projectArtifactPrefix,
                    cfg.groupId, cfg.projectArtifactPrefix, cfg.groupId, cfg.projectArtifactPrefix, module);
            case "web" -> """
                            <dependency>
                                <groupId>%s</groupId>
                                <artifactId>%s-%s-core</artifactId>
                            </dependency>
                            <dependency>
                                <groupId>%s</groupId>
                                <artifactId>%s-%s-biz</artifactId>
                            </dependency>
                            <dependency>
                                <groupId>%s</groupId>
                                <artifactId>%s-web</artifactId>
                            </dependency>
                            <dependency>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-starter-web</artifactId>
                            </dependency>
                            <dependency>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-starter-validation</artifactId>
                            </dependency>
                            <dependency>
                                <groupId>org.springdoc</groupId>
                                <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
                            </dependency>
                            <dependency>
                                <groupId>cn.hutool</groupId>
                                <artifactId>hutool-all</artifactId>
                            </dependency>
                            <dependency>
                                <groupId>com.fasterxml.jackson.core</groupId>
                                <artifactId>jackson-annotations</artifactId>
                                <scope>provided</scope>
                            </dependency>
                        """.formatted(cfg.groupId, cfg.projectArtifactPrefix, module,
                    cfg.groupId, cfg.projectArtifactPrefix, module, cfg.groupId, cfg.projectArtifactPrefix);
            default -> throw new IllegalArgumentException("未知层组: " + group);
        };
        return pomHeader() + """
                    <parent>
                        <groupId>%s</groupId>
                        <artifactId>%s-%s</artifactId>
                        <version>0.1.0-SNAPSHOT</version>
                        <relativePath>../pom.xml</relativePath>
                    </parent>

                    <artifactId>%s-%s-%s</artifactId>
                    <name>%s-%s-%s</name>
                    <description>业务模块 %s 的 %s 层子模块</description>

                    <dependencies>
                %s
                    </dependencies>
                </project>
                """.formatted(cfg.groupId, cfg.projectArtifactPrefix, module,
                cfg.projectArtifactPrefix, module, group, cfg.projectArtifactPrefix, module, group,
                module, group, deps);
    }

    private String replace(String text) {
        String out = text;
        out = out.replace(TOKENS.get(0)[0], cfg.projectPrefix + "Application");
        out = out.replace(TOKENS.get(1)[0], cfg.basePackage());
        // 目录路径用斜杠分隔，补一组路径版 token，避免 com/jakt 残留在目录结构中
        out = out.replace(TOKENS.get(2)[0], cfg.packagePath());
        // 工具前缀与项目名一致（2.0 移除 toolPrefix 配置项）
        out = out.replace(TOKENS.get(3)[0], cfg.projectPrefix);
        out = out.replace(TOKENS.get(4)[0], cfg.projectArtifactPrefix);
        out = out.replace(TOKENS.get(5)[0], cfg.groupId);
        out = out.replace(TOKENS.get(6)[0], cfg.groupId.replace('.', '/'));
        return out;
    }
}
