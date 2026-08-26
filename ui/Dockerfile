# syntax=docker/dockerfile:1
# 阶段1：构建生成器 fat jar（前端 java -jar 调用，见 ui/server.js runGenerator）
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
# Maven 换阿里云源（国内网络访问中央仓库慢/不稳）
RUN mkdir -p /root/.m2 \
    && printf '%s' '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd"><mirrors><mirror><id>aliyun</id><mirrorOf>*</mirrorOf><url>https://maven.aliyun.com/repository/public</url></mirror></mirrors></settings>' > /root/.m2/settings.xml
# 依赖层：只 COPY pom.xml，先离线下载依赖（改代码不重跑下载）；--mount=type=cache 跨构建缓存 ~/.m2
COPY generator/pom.xml generator/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -q -f generator/pom.xml -DskipTests dependency:go-offline
# 代码层：COPY 源码后编译，依赖已在本地仓库，只花编译时间
COPY generator/src generator/src
RUN --mount=type=cache,target=/root/.m2 mvn -q -f generator/pom.xml -DskipTests package

# 阶段2：运行环境 = Node（常驻 UI 服务）+ JDK17（前端调生成器用）
FROM node:20-bookworm-slim
# apt 换阿里云源（deb.debian.org 在国内网络不可达）
RUN sed -i "s|deb.debian.org|mirrors.aliyun.com|g; s|security.debian.org|mirrors.aliyun.com|g" /etc/apt/sources.list.d/debian.sources \
    && apt-get update \
    && apt-get install -y --no-install-recommends openjdk-17-jre-headless \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# UI 依赖（独立层，利用构建缓存）；npm 走 npmmirror 国内源；--mount=type=cache 跨构建缓存 ~/.npm
COPY ui/package.json ui/package-lock.json ./ui/
RUN --mount=type=cache,target=/root/.npm npm config set registry https://registry.npmmirror.com && cd ui && npm ci --omit=dev --no-audit --no-fund

# 整个仓库作为 TEMPLATE_ROOT（server.js: path.join(__dirname, '..')）
# .dockerignore 已排除 node_modules / target / .git / ~ 等
COPY . .

# 生成器 jar（server.js 固定读 /app/generator/target/generator.jar）
COPY --from=builder /build/generator/target/generator.jar /app/generator/target/generator.jar

ENV PORT=5180
EXPOSE 5180

CMD ["node", "ui/server.js"]
