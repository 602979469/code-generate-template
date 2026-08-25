# AGENTS.md — aiplatform 项目约定

> 本文件是 AI 编码代理在 aiplatform 仓库中新增、修改业务代码的唯一行为契约。
> 代码生成器自身实现不受本文件约束，但生成产物进入仓库前必须按本文件修剪。
> 规则约束的是概念，不是某一种字符串写法。用等价写法绕过同样违规。

## 0. 目标

本项目是 SOFABoot 风格的多模块 DDD 样板。业务代码应当“傻、平、可预测”：

- 业务规则只出现在该出现的层；
- 持久化、日志、异常、分页、转换等横切能力只有统一出口；
- 换一个 AI 也能根据本文件写出几乎相同的实现。

## 1. 技术栈

- Java 17
- Spring Boot 4.0.6
- MyBatis 4.0.1（interface + XML）
- MySQL 8
- Redis
- Sa-Token 1.45.0
- springdoc 3.0.3
- Lombok
- Hutool

不使用 MyBatis-Plus，不使用业务代码里的 `@Transactional`。

## 2. 模块与依赖方向

### 2.1 模块树

```text
web/                       → aiplatform-web
biz/service-impl           → aiplatform-biz-service-impl
core/model                 → aiplatform-core-model
core/repository            → aiplatform-core-repository
core/service               → aiplatform-core-service
common/dal                 → aiplatform-common-dal
common/framework           → aiplatform-common-framework
common/util                → aiplatform-common-util
common/integration         → aiplatform-common-integration
bootstrap                  → aiplatform-bootstrap
```

### 2.2 依赖方向

```text
common-util
    ↑
    ├── common-framework
    ├── core-model
    ├── common-dal
    ├── common-integration
    │
    core-model      → common-framework + common-util
    core-repository → core-model + common-dal + common-util
    core-service    → core-model + core-repository + common-util + common-integration
    biz-service-impl → core-model + core-service + common-util
    web             → biz-service-impl + core-model + common-util + common-framework
    bootstrap       → 以上所有模块
```

规则：

- 唯一依赖判据是目标模块 `pom.xml` 是否显式声明来源模块；
- 依赖传递“偷渡”违规；
- 禁止反向依赖、循环依赖；
- `web` 不得直接依赖 `common-dal`、`core-repository`；
- `core-model` 不得依赖 Spring、MyBatis、Redis；
- `core-service` 不得直接依赖 `common-dal`；
- `common-util` 是最底层基础工具，不依赖任何内部业务模块。

### 2.3 各模块职责

| 模块 | 只允许出现 | 禁止出现 |
|---|---|---|
| common-util | ErrorCode、CommonErrorCode、CommonException、Result、PageResult、LogFileEnum、LoggerUtil、AssertUtil、ConvertUtil、ParamValidator、TransactionTemplate、BizTemplate、ClientInfoUtil、ThreadPoolUtil、JsonUtil、TraceIdUtil、基础配置 | core-model、common-dal、web、biz、业务规则 |
| common-framework | BaseModel、BaseEnum、ErrorCodeEnum、AiPlatformException、UserContext、PageParam、AiPlatformConstants、插件代码 | core-model、common-dal、web、biz、业务规则 |
| core-model | 业务 domain、业务 param、业务 enums（每表生成） | 公共类、Spring/MyBatis/Redis、业务服务实现、持久化细节 |
| common-dal | DO、Mapper、Mapper.xml、DalQuery、DalResult、RedisClient、持久化连接配置 | core-model、业务规则、web/biz 类型 |
| common-integration | 外部 HTTP/RPC 客户端、集成异常、集成配置 | core-model、common-dal、业务规则 |
| core-repository | Repository、RepositoryImpl、Convertor | 业务规则、对外暴露 DO/DalQuery/DalResult |
| core-service | Service、ServiceImpl、BizChecker | common-dal、web DTO、直接调 Mapper、业务外编排 |
| biz-service-impl | Manager、ManagerImpl、必要的 shared 接口 | core-repository、common-dal、DTO 组装、Manager 互调 |
| web | Controller、DTO、Assembler、ParamChecker、ApiTemplate、ApiResult、web 配置 | common-dal、core-repository、业务规则 |
| bootstrap | 启动类、Bean 装配、配置 | 业务代码 |

## 3. AI 编码行为守则

1. 修改代码前完整阅读本文件。
2. 不发明新写法。规则未覆盖时，找同仓最接近的合规实现照抄，并说明依据。
3. 禁止等价绕过。禁止概念的替代写法同样违规。
4. 动笔前先搜索同仓已有的枚举、错误码、常量、工具。
5. 遇到通用能力先查 common-util，再查 Hutool；仍不满足时评估 Apache Commons、Guava、Jackson 等成熟库。
6. 优先简单实现，不为了“显得高级”引入不必要抽象。
7. 提交前逐项通过第 9 节自查清单。

## 4. 统一出口

| 关注点 | 唯一出口 | 禁止 |
|---|---|---|
| 条件断言 | `AssertUtil.throwErrWhenXxx`，必须显式传 ErrorCode | 手写 `if (...) { throw ... }` |
| 无条件业务失败 | `throw AiPlatformException.ofThrow(ErrorCodeEnum.XXX[, message])` | `throw new AiPlatformException`、其他异常类 |
| 判空 | Hutool `ObjectUtil/StrUtil/CollUtil/ArrayUtil/MapUtil` | 业务代码手写 `== null`、`isEmpty()` |
| 日志 | `LoggerUtil` + `LogFileEnum` | `LoggerFactory`、`System.out`、`printStackTrace`、空 catch |
| DTO/Model/DO 转换 | `XxxAssembler`、`XxxConvertor` | 业务层 `new XxxModel()` + setter |
| 多写事务 | `BizTemplate.execute(transactionTemplate, callback)` | `@Transactional`、裸调多个写 |
| 客户端信息 | `ClientInfoUtil` | 业务代码读 Header |
| 分页 | `PageQueryRequest → XxxQueryParam → DalPageQuery → PageResult → ConvertUtil.mapPage` | 裸参数分页、自定义分页字段 |
| 接口返回 | web `ApiResult<T>` + `ApiTemplate` | 返回裸对象 |

### 4.1 判空豁免

只有以下三处允许手写 `== null`：

1. 转换方法首行：空返回 null / 空参数；
2. 查询类 ParamChecker 的 request 为空跳过；
3. `AssertUtil` 参数内部已经封装的判空表达式。

其余场景一律使用 Hutool。

### 4.2 嵌套调用

自定义 Bean/业务对象方法（Manager/Service/Repository/Convertor 等）的返回值必须先赋值给局部变量再使用。

允许嵌套的只有：

- Java 标准库；
- Hutool；
- 项目静态工厂；
- getter；
- stream 链。

尤其禁止在 `AssertUtil` 的条件参数里直接调用 Repository/Service/Manager/Convertor。

## 5. 各层写法

### 5.1 web

`Controller` 只做：

```text
收参 → ParamChecker → Manager → Assembler → ApiTemplate
```

规则：

- 所有 Controller 统一走 `ApiTemplate.execute` 或 `executeWithoutResult`；
- 参数校验统一在 `beforeService` 中调用 `ParamChecker`；
- Controller 方法参数禁止使用 `@Valid`；
- 查询类 request 为空允许，由 Assembler 转默认查询参数；
- 强校验类 request 为空，ParamChecker 第一行抛 `PARAM_INVALID`；
- 无参用例不重写 `beforeService`；
- 二进制下载、文件流等无法包 `ApiResult` 的接口，必须在 Controller 注释说明原因；
- Controller 禁止业务逻辑、try-catch、`new Model`、读 Header、拼查询参数；
- 原生 Spring 对象读取、Response、Header、MDC 等统一收进工具类。

`ParamChecker`：

- 静态 final class；
- 只做请求级校验：空值、长度、枚举值、互斥字段；
- 不判断 id 是否存在、角色是否合法等业务问题；
- 失败抛 `CommonException(PARAM_INVALID, message)`。

`Assembler`：

- 静态 final class；
- 只做字段搬运和类型转换；
- 空入参返回 null 或空对象；
- 列表/分页转换统一 `ConvertUtil.map/mapPage`。

`ApiTemplate` 异常处理：

- 参数校验异常 → `ApiResult.fail(PARAM_INVALID, message)`；
- 业务异常 → 对应 `errorCode`，不记日志；
- 集成异常 → common-integration 已记日志，这里只返回 code/message；
- 未知异常 → `LoggerUtil.error(COMMON_ERROR, ...)` + `SYSTEM_ERROR`。

### 5.2 biz

`XxxManager` + `XxxManagerImpl`：

- 基本与 Controller 一一对应；
- 输入输出使用 core-model 领域对象；
- 只做用例编排，调用多个 core-service；
- 不碰仓储、不碰 Mapper、不做 DTO 组装；
- Manager 之间禁止互相调用；
- 跨 Manager 共享能力放 `biz.service.shared`，无需求不建。

### 5.3 core-service

`XxxService` + `XxxServiceImpl`：

- 承载业务规则；
- 允许创建领域对象；
- 禁止 DTO↔Model 转换；
- 禁止 import common-dal；
- 条件失败用 `AssertUtil`；
- 无条件失败用 `AiPlatformException.ofThrow`；
- 相同语义复用已有 `ErrorCodeEnum`，新语义新增错误码。

`BizChecker`：

- 位于 `core.service.checker`；
- 命名：`XxxService` → `XxxBizChecker`；
- Spring Bean，可以注入 Repository；
- 方法统一 `void`；
- 禁止返回数据、禁止回填；
- 可以提供 `checkXxx(id)` 和 `checkXxx(entity)` 重载；
- 其他 core-service 可复用。

事务：

- 先查询、校验，再开事务；
- 单表单写直接调 Repository；
- 一个用例发生超过一次 INSERT/UPDATE/DELETE，必须使用：

```java
BizTemplate.execute(transactionTemplate, callback);
```

- 事务内先执行仓储 SQL，再执行非仓储操作；
- 非事务副作用尽量放事务提交后，或做幂等/补偿；
- 禁止把所有操作无脑塞进事务。

### 5.4 core-repository

`XxxRepository` + `XxxRepositoryImpl`：

- 全仓唯一允许碰 Mapper、DO、Redis 的领域层；
- 输入输出一律 core-model；
- 负责 `core-model QueryParam → DalQuery`；
- 负责 `DO/DalResult → Model`；
- 转换代码必须写在 `XxxConvertor`；
- Convertor 之间允许互相调用。

仓储返回值：

- `findOne`：由 Mapper `selectOne` 实现，返回单个 core-model；多条由 MyBatis 抛 `TooManyResultsException`，不做特殊处理；
- `findList/findPage`：返回 `List` / `PageResult<core-model>`；
- `insert`：按表主键类型返回主键，复合主键或无回填返回 `int`；
- `update/updateByCondition/delete`：返回 `int` 受影响行数，0 只表示未生效，由上层决定；
- 除底层 SQL 异常外，不主动抛业务异常。

### 5.5 common-dal

- Mapper 接口 + XML，所有 SQL 在 XML；
- 禁止 `@Select/@Insert/@Update/@Delete` SQL 注解；
- 单表查询用 `resultType="XxxDO"`；
- 多表/聚合查询用 `resultType="XxxDalResult"`；
- 不使用复杂 resultMap；
- 查询参数使用 `XxxDalQuery`，字段为数据库原始类型；
- 分页参数使用 `DalPageQuery`；
- XML 直接引用 DalQuery 字段，不出现 `.code`；
- Redis 只提供通用 KV 方法，业务 key 由上层传入；
- 不写业务 Redis key 常量；
- 不 import core-model。

### 5.6 common-util

`common-util` 是最底层基础模块，不依赖内部业务模块。

核心类型：

- `ErrorCode`
- `CommonErrorCode`
- `CommonException`
- `Result<T>` / `PageResult<T>`
- `LogFileEnum`
- `LoggerUtil`
- `AssertUtil`
- `ConvertUtil`
- `ParamValidator`
- `TransactionTemplate`
- `BizTemplate`
- `ClientInfoUtil`
- `ThreadPoolUtil`
- `JsonUtil`
- `TraceIdUtil`

### 5.7 common-integration

- 只放外部 HTTP/RPC 客户端；
- 依赖 common-util，不依赖 core-model/common-dal；
- 集成异常 `AiIntegrationException extends CommonException`；
- 所有集成异常必须在 common-integration 内打日志，使用 `LogFileEnum.INTEGRATION`；
- 集成错误码用枚举，`getCode()` 返回枚举名。

### 5.8 bootstrap

- 唯一可启动模块；
- 负责 Bean 装配和配置；
- 不写业务代码。

## 6. 命名与包结构

### 6.1 类名

- `XxxController`
- `XxxManager` / `XxxManagerImpl`
- `XxxService` / `XxxServiceImpl`
- `XxxBizChecker`
- `XxxRepository` / `XxxRepositoryImpl`
- `XxxConvertor`
- `XxxMapper`
- `XxxDO`
- `XxxDalQuery`
- `XxxDalResult`
- `Xxx` Model
- `XxxRequest` / `XxxResponse`
- `XxxParamChecker`
- `XxxAssembler`

### 6.2 包结构

`core-repository`

- `core.repository`
- `core.repository.impl`
- `core.repository.convertor`

`core-service`

- `core.service`
- `core.service.impl`
- `core.service.checker`

`biz-service-impl`

- `biz.service`
- `biz.service.impl`
- 可选 `biz.service.shared`

`web`

- `controller`
- `param`
- `result`
- `assembler`
- `checker`
- `template`
- `config`

`common-dal`

- `dataobject`
- `mapper`
- `query`
- `redis`
- `config`

## 7. 枚举、常量与魔法值

- 状态/类型字段在 core-model 用枚举；
- DO 保持数据库原始类型，Model/DTO 用枚举，Convertor 转换；
- DalQuery 用数据库原始类型；
- 枚举由生成器生成自己的 `fromCode` / `fromCodeJson`（`@JsonCreator` 反序列化入口）；
- 生成器配置中枚举列必须同步 `columns.type: enum`；
- 业务 key、默认值、角色 key、权限码等收口 `core-model.constant`；
- 错误码集中 `ErrorCodeEnum`；
- 日志枚举 `LogFileEnum` 放 common-util；
- 领域语义值必须有名字，禁止散落魔法值。

## 8. 代码风格

- 构造器注入 + `final` 字段，禁止字段注入、`@Autowired`；
- 显式类型，禁止 `var`；
- 禁止断言参数取反，例如 `throwErrWhenFalse(!cond)`；
- 接口方法必须有 javadoc；
- Manager 接口方法带 `@param` / `@return`；
- `@Override` 方法免注释；
- 非 `@Override` 方法，含 private，必须有 javadoc；一行私有构造器豁免；
- 禁止空 catch、吞异常；
- 业务异常不记日志；系统异常、集成异常记日志；
- traceId、userId 等上下文由 MDC 自动带，不靠业务代码手动拼；
- 敏感数据严禁打印，严禁 Controller 直接返回。

## 9. 提交前自查清单

1. 我是否新增了抛异常方式？
2. 我是否手写判空，且不在三处豁免内？
3. 我是否在断言/三元表达式里直接调用了业务 Bean 方法？
4. 我是否使用了 `!` 翻转断言？
5. 我是否新增了魔法值？
6. 我是否 import 了本模块 pom 未显式声明的模块？
7. 我是否在非允许层手写 `new Model + setter`？
8. 我是否绕过了 Repository 直接碰 Mapper/DO/Redis？
9. 我是否重复造了已有工具的能力？
10. 我的用例是否发生多次写操作？是则必须走 `BizTemplate`。
11. 我的命名、注释、包结构是否符合本文件？
12. 我是否照抄了同仓已有合规实现，而不是自创写法？

## 10. 禁止概念速查表

| 禁止概念 | 判断标准 | 合规替代 |
|---|---|---|
| 手写 if + throw | 业务代码出现 `if (...) throw` | `AssertUtil` 或 `AiPlatformException.ofThrow` |
| 手写判空 | 非豁免场景出现 `== null`、`isEmpty()` | Hutool |
| 手写日志 | `LoggerFactory`、`System.out`、`printStackTrace` | `LoggerUtil` |
| 空 catch | catch 块无日志无处理 | 分类处理并记日志 |
| 业务层碰数据源 | core-service/web 出现 DO、Mapper、DalQuery、Redis | 收口 Repository |
| core-model 引框架 | 出现 Spring/MyBatis/Redis import | 仅领域语义 + common-util |
| 字符串裸错误 | 返回裸字符串、`Result.fail("...")` | ErrorCode + ApiResult |
| @Transactional | 业务模块使用 | `BizTemplate.execute(transactionTemplate, callback)` |
| @Valid | Controller 方法参数使用 | ParamChecker + ParamValidator |
| 敏感信息泄漏 | 日志/响应/查询出现 password/token/apiKey 明文 | 剔除或脱敏 |
| 循环/反向依赖 | pom 方向与本文不符 | 调整模块归属 |
| Controller 写业务 | try-catch、规则判断、new Model、读 Header | 下沉 Manager/Service/Assembler/工具 |

## 11. 新功能接入流程

新增一个业务表，按以下顺序落地：

1. 在 `generate.yaml` 中配置表信息；枚举列必须配置 `columns.type: enum`；
2. `common-dal`：`XxxDO`、`XxxMapper`、`XxxMapper.xml`、`XxxDalQuery`，必要时 `XxxDalResult`；
3. `core-model`：`Xxx`、`XxxQueryParam`、必要枚举、常量；
4. `core-repository`：`XxxRepository`、`XxxRepositoryImpl`、`XxxConvertor`；
5. `core-service`：`XxxService`、`XxxServiceImpl`、`XxxBizChecker`；
6. `biz-service-impl`：`XxxManager`、`XxxManagerImpl`；
7. `web`：`XxxRequest/Response`、`XxxParamChecker`、`XxxAssembler`、`XxxController`；
8. 生成产物按本文件修剪，删除无用文件，调整包名和命名。

生成代码的已知语义：

- `insert` 是全字段插入，不提供部分插入；
- `update` 是全量更新；
- `updateByCondition` 按非空字段条件更新；
- `findOne` 由 Mapper `selectOne` 实现，零条返回 null，多条由 MyBatis 抛 `TooManyResultsException`（不额外处理）；
- 敏感字段不得出现在响应、查询和日志中。

## 12. 测试约定

- 生成器不生成测试代码；
- 业务模块不写 Mockito 单元测试；
- 后期测试统一放独立测试模块，连真实测试数据库，一路打到 Mapper。
