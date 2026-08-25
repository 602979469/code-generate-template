# AGENTS.md — 代码生成器与开发者行为军规

> 版本：v2.0 | 生效日期：2026-08-25 | 适用范围：所有开发人员
> 参考基线：aiplatform（1.0）约定 +《03-代码生成规范》。规则约束的是概念，用等价写法绕过同样违规。

v2 相对 v1 的调整：模块名对齐 common-util / common-dal / common-framework / common-integration；应用服务层统一 XxxManager；异常统一 BizException，公共异常码归 common-framework；新增"统一出口""判空豁免""代码风格""敏感数据""提交前自查""禁止概念速查"等约束。

---

## 一、模块隔离红线（Maven 依赖级）

| 编号 | 规则 | 违规后果 |
| :--- | :--- | :--- |
| M-001 | core-service / biz-service **不得包含** common-dal 依赖 | 编译不通过 |
| M-002 | core-model **只允许依赖 common-util / common-framework**，禁止依赖 Spring/MyBatis/Redis | 编译不通过 |
| M-003 | common-framework **不得依赖** 任何 core-/biz- 模块 | 编译不通过 |
| M-004 | common-integration **禁止依赖 core-model/common-dal**，集成结果由上层 Convertor 转换 | CR 打回 |
| M-005 | web **不得直接依赖** common-dal / core-repository | CR 打回 |
| M-006 | 依赖传递"偷渡"违规（唯一判据是目标模块 pom.xml 是否显式声明来源模块） | CR 打回 |

---

## 二、分层职责红线

| 层级 | 禁止事项 |
| :--- | :--- |
| **Controller** | 只做"收参 → ParamChecker → Manager → Assembler → ApiTemplate"；禁止 if-else 业务逻辑、try-catch、new Model、读 Header、`@Valid` |
| **ParamChecker** | 只做请求级校验（空值/长度/枚举/互斥字段），禁止判断 id 是否存在等业务问题 |
| **Assembler** | 只做字段搬运和类型转换；空入参返回 null / 空对象；敏感列不进响应 |
| **Manager** | 只做用例编排；禁止写具体业务规则、禁止碰仓储/Mapper |
| **Service** | 承载业务规则；禁止注入 Mapper、禁止 DTO↔Model 转换、禁止依赖 common-dal |
| **Repository** | 输入输出一律 core-model；禁止返回 DO/DalQuery/DalResult、禁止业务逻辑判断 |
| **Convertor** | 转换代码必须写在 Convertor/Assembler，禁止业务层 `new Model + setter` |
| **Model** | 禁止 Spring/MyBatis 注解；DO 保持数据库原始类型（"DO 永不漂移"），Model/DTO 使用枚举 |

---

## 三、异常与错误码红线

| 编号 | 规则 |
| :--- | :--- |
| E-001 | 业务异常统一 `BizException` + ErrorCode，禁止自定义异常类、禁止手写 `if (...) throw` |
| E-002 | 技术类异常使用 `CommonErrorCode`，业务类异常使用业务域 `ErrorCodeEnum`；**公共异常码放 common-framework** |
| E-003 | 禁止在全局异常拦截器中对 code 做存在性校验 |
| E-004 | 条件断言统一 `AssertUtil.throwErrWhenXxx`（必须显式传 ErrorCode），禁止断言参数取反（如 `throwErrWhenFalse(!cond)`） |
| E-005 | 禁止空 catch、禁止吞异常；业务异常不记日志，系统/集成异常记日志 |

---

## 四、统一出口（v2 新增）

| 关注点 | 唯一出口 | 禁止 |
| :--- | :--- | :--- |
| 条件断言 | `AssertUtil.throwErrWhenXxx`（显式传 ErrorCode） | 手写 `if (...) throw` |
| 无条件业务失败 | `BizException.ofThrow(ErrorCodeEnum.XXX)` | `new BizException`、其他异常类 |
| 判空 | Hutool `ObjectUtil/StrUtil/CollUtil/ArrayUtil/MapUtil` | 业务代码手写 `== null`、`isEmpty()` |
| 日志 | `LoggerUtil` + `LogFileEnum` | `LoggerFactory`、`System.out`、`printStackTrace`、空 catch |
| DTO/Model/DO 转换 | `XxxAssembler`（web）/ `XxxConvertor`（repository） | 业务层 `new XxxModel` + setter |
| 多写事务 | `BizTemplate.execute(transactionTemplate, callback)` | `@Transactional`、裸调多个写 |
| 客户端信息 | `ClientInfoUtil` | 业务代码读 Header |
| 分页 | `XxxQueryRequest → XxxQueryParam → XxxDalQuery → PageResult → ConvertUtil.mapPage` | 裸参数分页、自定义分页字段 |
| 接口返回 | `ApiResult<T>` + `ApiTemplate` | 返回裸对象 |

### 判空豁免（全仓仅三处）

1. 转换方法首行：空返回 null / 空参数；
2. 查询类 ParamChecker 的 request 为空跳过；
3. `AssertUtil` 参数内部已封装的判空表达式。

其余场景一律使用 Hutool。

---

## 五、代码风格（v2 新增）

- 构造器注入 + `final` 字段，禁止字段注入、`@Autowired`；
- 显式类型，禁止 `var`；
- 接口方法必须有 javadoc；Manager 接口方法带 `@param` / `@return`；`@Override` 方法免注释；非 `@Override` 方法（含 private）必须有 javadoc；
- 状态/类型字段在 core-model 用枚举；DO 保持数据库原始类型，Model/DTO 用枚举，Convertor 转换；
- 业务 key、默认值、角色 key、权限码收口 `core-model.constant`，禁止散落魔法值；
- 敏感数据严禁打印、严禁 Controller 直接返回；
- 不发明新写法：规则未覆盖时找同仓最接近的合规实现照抄，并说明依据。

---

## 六、Code Review 必查清单（v2）

- [ ] Controller 是否有业务逻辑 / try-catch / `new Model` / `@Valid`？
- [ ] Controller 是否直接注入了 core-service / Repository？
- [ ] 是否统一走 `ApiTemplate.execute` / `executeWithoutResult`？
- [ ] 参数校验是否在 ParamChecker（`beforeService`）完成？
- [ ] Manager 是否直接碰了仓储/Mapper？
- [ ] Service 是否注入了 Mapper 或依赖了 common-dal？
- [ ] Repository 是否将 DO / DalQuery / DalResult 返回给了上层？
- [ ] 转换是否都走 Assembler / Convertor？
- [ ] 判空是否手写且不在三处豁免内？
- [ ] 多写用例是否走 `BizTemplate`？
- [ ] 敏感字段是否出现在响应 / 查询条件 / 日志中？
- [ ] 接口方法 javadoc、`@Override` 是否符合规范？

---

## 七、提交前自查清单（v2 新增）

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

---

## 八、禁止概念速查表（v2 新增）

| 禁止概念 | 判断标准 | 合规替代 |
| :--- | :--- | :--- |
| 手写 if + throw | 业务代码出现 `if (...) throw` | `AssertUtil` 或 `BizException.ofThrow` |
| 手写判空 | 非豁免场景出现 `== null`、`isEmpty()` | Hutool |
| 手写日志 | `LoggerFactory`、`System.out`、`printStackTrace` | `LoggerUtil` |
| 空 catch | catch 块无日志无处理 | 分类处理并记日志 |
| 业务层碰数据源 | core-service/web 出现 DO、Mapper、DalQuery、Redis | 收口 Repository |
| core-model 引框架 | 出现 Spring/MyBatis/Redis import | 仅领域语义 + common-util/common-framework |
| 字符串裸错误 | 返回裸字符串、`Result.fail("...")` | ErrorCode + ApiResult |
| @Transactional | 业务模块使用 | `BizTemplate.execute(transactionTemplate, callback)` |
| @Valid | Controller 方法参数使用 | ParamChecker + ParamValidator |
| 敏感信息泄漏 | 日志/响应/查询出现 password/token/apiKey 明文 | 剔除或脱敏 |
| 循环/反向依赖 | pom 方向与本文不符 | 调整模块归属 |
| Controller 写业务 | try-catch、规则判断、new Model、读 Header | 下沉 Manager/Service/Assembler/工具 |
