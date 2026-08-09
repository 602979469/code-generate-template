# AGENTS.md — 给 AI 编码代理的项目约定

> 修改代码前，请先完整阅读本文件与 `docs/` 下的规范文档。本项目以"规范约束"为第一目标，违反约束的代码即使能跑也算缺陷。

## 项目定位

SOFABoot 风格的多模块 DDD 样板。业务代码应当是"傻、平、可预测"的：复杂度和规则全部收进底层模块，业务层只做组装与调用。

## 技术栈

Java 17、Spring Boot 4.0.6、MyBatis 4.0.1（interface + XML）、MySQL 8、springdoc 3.0.3。领域模型使用 Lombok @Data（core-model 已引入），不使用 MyBatis-Plus、`@Transactional`。

## 模块与依赖方向（红线）

目录按树形结构组织，叶子节点是 Maven 模块（目录名 ≠ artifactId）：

```
web/            → aiplatform-web
biz/ service-impl → aiplatform-biz-service-impl
core/ model     → aiplatform-core-model
core/ repository → aiplatform-core-repository
core/ service   → aiplatform-core-service
common/ dal     → aiplatform-common-dal
common/ util    → aiplatform-common-util
common/ integration → aiplatform-common-integration
bootstrap       → aiplatform-bootstrap
```

```
aiplatform-bootstrap → web → biz-service-impl → core-service → core-repository → common-dal
                                        ↘                                     ↗
                              core-model（零依赖，所有人依赖）  common-util / common-integration
```

- `core-model`：领域模型、查询参数、AiPlatformException/ErrorCodeEnum、Result/PageResult、BizTemplate。仅依赖 lombok/slf4j。
- `common-integration`：外部服务集成（预留）。
- `common-dal`：MyBatis Mapper（interface + XML）、DO。
- `common-util`：工具（TraceIdUtil、RestTemplateConfig、AiPlatformInvoker、线程池配置与调用工具）。
- `core-repository`：封装 Mapper，DO→Model，可组合多个 Mapper，当前阶段单表操作不引入事务。
- `core-service`：领域服务，承载业务规则。
- `biz-service-impl`：BizService，用例编排，输入输出都是领域模型。
- `web`：Controller、DTO、全局异常处理、日志切面。
- `bootstrap`：MainApplication + 注解扫描 + 配置文件，唯一可启动模块。

禁止：反向/循环依赖；`web` 直接依赖 `common-dal` 或 `core-repository`。

## 核心约定（速查）

1. 所有接口返回 `Result<T>`（core-model result 包定义），禁止返回裸对象。
2. 业务异常一律抛 `AiPlatformException(ErrorCodeEnum.XXX)`（core-model 定义），禁止字符串错误码；条件校验统一用 `AiPlatformInvoker`（throwErrWhenNull/throwErrWhenBlank/throwErrWhenEmpty/throwErrWhenTrue 等），禁止手写 `if (xx) { throw ... }`。
3. 判空/判 blank 统一用 Hutool（`StrUtil`/`CollUtil`/`ArrayUtil`/`ObjectUtil`），禁止手写 null/empty 判断。
4. 事务：禁止 `@Transactional` 注解。当前阶段项目不使用 TransactionTemplate（已移除），单表操作直接调 Mapper；后续出现跨表复杂用例时再引入事务工具。
5. 参数校验：DTO 注解 + Controller `@Valid`；业务规则在 `core-service` 编码校验后抛 `AiPlatformException`。
6. 日志：禁止业务代码手写 try-catch 打日志；关键节点 `logger.info`；traceId 自动写入 MDC。
7. 命名：`XxxController`、`XxxBizService`、`XxxDomainService`、`XxxRepository`、`XxxMapper`、`XxxDO`（common-dal）、`Xxx` Model（core-model）、`XxxRequest`/`XxxResponse`（web DTO）。
7. 代码风格：构造器注入 + `final` 字段；DTO 优先 record（GET 查询绑定用普通类）；领域模型用 Lombok @Data。

## 新增一个业务模块（以 Order 为例）

1. `sql/init.sql` 建表；
2. `aiplatform-common-dal`：`OrderDO` + `OrderMapper`（interface）+ `resources/mapper/OrderMapper.xml`；
3. `aiplatform-core-model`：`Order` Model、`OrderQueryParam` 查询参数；
4. `aiplatform-core-repository`：`OrderRepository`（封装 Mapper，DO→Model，单表操作直接调 Mapper）；
5. `aiplatform-core-service`：`OrderDomainService`（业务规则）；
6. `aiplatform-biz-service-impl`：`OrderBizService`（编排，输入输出 Model）；
7. `aiplatform-web`：`OrderCreateRequest`/`OrderResponse` DTO + `OrderController`；
8. 对照 `User` 模块写领域规则单元测试（Mockito 桩仓储）。

## 禁止模式

- Controller 里写业务逻辑或 try-catch；
- BizService/Repository 直接用 `@Transactional`；
- Service 直接调 Mapper（必须经过 Repository）；
- `core-model` 出现任何 Spring/MyBatis 依赖；
- 吞异常、空 catch、`catch (Exception e) {}`；
- 打印密码、token 等敏感信息；
- 在 core-service 里拼 SQL、在 web 里写业务规则。
