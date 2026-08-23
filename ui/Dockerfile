# 阶段1：构建生成器 fat jar（前端 java -jar 调用，见 ui/server.js runGenerator）
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY generator/pom.xml generator/pom.xml
COPY generator/src generator/src
RUN mvn -q -f generator/pom.xml -DskipTests package

# 阶段2：运行环境 = Node（常驻 UI 服务）+ JDK17（前端调生成器用）
FROM node:20-bookworm-slim
RUN apt-get update \
    && apt-get install -y --no-install-recommends openjdk-17-jre-headless \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# UI 依赖（独立层，利用构建缓存）
COPY ui/package.json ui/package-lock.json ./ui/
RUN cd ui && npm ci --omit=dev --no-audit --no-fund

# 整个仓库作为 TEMPLATE_ROOT（server.js: path.join(__dirname, '..')）
# .dockerignore 已排除 node_modules / target / .git / ~ 等
COPY . .

# 生成器 jar（server.js 固定读 /app/generator/target/generator.jar）
COPY --from=builder /build/generator/target/generator.jar /app/generator/target/generator.jar

ENV PORT=5180
EXPOSE 5180

CMD ["node", "ui/server.js"]
