# code-generate-template 测试执行报告

> 对应测试分析：[test-analysis-report.md](test-analysis-report.md)
> 执行日期：2026-08-10
> 测试库：`aitest`（MySQL 9.7.1，root/123456）
> 测试输出项目：`/Users/jakt/IdeaProjects/aitest`（生成器产物，`mvn package` 编译通过）
> 执行方式：shell 断言 + Maven 编译 + Spring Boot 启动 + HTTP 冒烟 + MySQL 直查

---

## 1. 执行概览

| 阶段 | 结果 |
| --- | --- |
| 测试环境准备（建库建表灌数据） | ✅ 6 张表：example / sys_dept / sys_job_log / member / json_test / dict_item |
| 全功能生成（generateExample + 4 张表） | ✅ 骨架 50 文件；example 21、sys_dept 19、sys_job_log 14（12+2 枚举）、member 19；无报告框 |
| 产物静态断言 | ✅ 见 §2 |
| 场景化测试（重复运行 / force / 枚举冲突 / 负向配置） | ✅ 见 §3 |
| 编译验证 | ✅ `mvn -q package` 全模块通过（主项目）；❌ Long 枚举项目编译失败（缺陷 D1） |
| 运行冒烟（启动 + 接口全链路） | ✅ 启动 2s；接口用例见 §4；发现缺陷 D5/D6/D7 |
| 缺陷确认 | **8 项**（P1×1，P2×5，P3×2），见 §5 |

## 2. 产物静态断言结果

### 2.1 骨架初始化（SKEL-01~07）✅

- 目录结构完整（bootstrap/web/biz/core/common 多模块 + 各 pom）；
- token 替换正确：`AiTestApplication`、`com.jakt.aitest.*`、`AiTestException/AiTestInvoker/AiTestTemplate`、数据源 URL 自动变为 `/aitest`；全仓 grep 无 `com.jakt.aiplatform`/`AiPlatform*` 残留；
- Maven 占位符 `${java.version}` 等原样保留；
- 无 User 业务代码残留；
- 重复初始化：`生成 0 个文件，跳过 50 个`，不覆盖；
- `sql/example.sql` 随骨架复制；
- outputDir 不存在时自动创建。

### 2.2 类型映射与转换（TYPE 组）✅（除 D1）

- 默认映射矩阵实测：varchar/char/text→String、int/tinyint→Integer、bigint→Long、decimal→BigDecimal、double→Double、bit→Boolean、date→LocalDate、datetime→LocalDateTime；
- 枚举分层：`ExampleDO` 保持 `Integer userType / String status`，Model/DTO 为 `UserTypeEnum / UserStatusEnum`；`JobLogDO` 原始类型 vs `JobLog` 枚举类型；
- Convertor：`UserTypeEnum.fromCode`、`ObjectUtil.isNull(...) ? null : getCode()`、`JsonUtil.parseArray/parseObject/toJson`、Hutool `Convert.toInt/toLong`，无裸 `Integer.valueOf`；
- QueryParam/QueryRequest 保持原始 code 类型（`Integer userType / String status`）；
- 校验注解：`userName/password` @NotBlank+@Size(30/100)，`nickName` @Size(50)，可空列无 @NotNull；
- json 列不配 type → String 直传（extra 列）。

### 2.3 逻辑删除（LOGIC 组）✅

- 全局配置：example `deleteById` 为 `UPDATE ... SET del_flag = 1 WHERE id = ? AND del_flag = 0`，select/update 均带 `AND del_flag = 0`；
- 表级覆盖：member 的删除值变为 `del_flag = 2`（覆盖全局的 1）；
- 列不存在（sys_dept / sys_job_log）→ 退化物理删除 `<delete>`；
- `del_flag` 不进入 DO/Model/selectColumns/insertColumns/updateSet；
- insert 不写 del_flag，由 DB 默认 0。

### 2.4 内部表 / 防覆盖 / SQL / 报告（CRUD/OUT 组）✅

- `sys_job_log`（generateController:false）：无 Controller/ParamChecker/DTO/Assembler，仅 12 文件 + 2 枚举；
- 重复运行：4 张表全部跳过，报告框统计 `成功 0 / 跳过 4 / 警告 0` + 明细；
- force_create：sys_dept 覆盖 + ⚠️ 警告，报告框 `成功 1 / 跳过 2 / 警告 1`；
- 枚举冲突：删除 DO 后重跑 → `枚举 DictTypeEnum 已存在` 整表跳过（报告框正确）；
- 每表生成 `sql/{表名}.sql`（真实 SHOW CREATE TABLE）；
- 控制台无逐文件路径噪声；全部成功不画框。

### 2.5 负向配置批量（CFG 组）✅（2 项文案偏差见 D4）

13 个负向配置全部 fail-fast（退出码 1），错误信息准确：
缺 projectPrefix / 非法 projectArtifactPrefix（ai-prod）/ 缺 model_comment / 逻辑删除缺值 / 列不存在于表 / 枚举 code 重复 / jsonObject+String / 非法 type / 缺 jdbc（tables 与 generateExample 两种）/ 集合类型用于普通列 / 非法 codeType / 表不存在。

## 3. 场景化测试结果

| 场景 | 结果 | 说明 |
| --- | --- | --- |
| S1 重复运行 | ✅ | 4 跳过 + 报告框 |
| S2 force_create 混合 | ✅ | 1 成功 + 2 跳过 + 1 警告 + 报告框 |
| S3 枚举冲突 | ✅ | 首次 20 文件（19+枚举）→ 删除 DO 重跑整表跳过 → 恢复 |
| S4 最小回归（v1 形态） | ✅ | 19 文件、无生成枚举、无逻辑删除 SQL |
| S5 example 默认关闭 | ✅ | 无 Example/Tag/Profile 任何文件 |
| S6 负向配置批量 | ✅ | 13/13 fail-fast，信息准确（含 2 项文案偏差） |
| S7 Long 枚举 | ❌ | **编译失败（D1）** |
| S8 example 重复配置 | ❌ | **未报错，静默跳过（D3）** |
| S9 不存在表 | ❌ | **先落 19 个半成品文件再报错（D2）** |

## 4. 运行冒烟结果

应用启动：Spring Boot 4.0.6，`java -jar aitest-bootstrap-0.1.0-SNAPSHOT.jar --server.port=8081`，2s 启动成功。

| 用例 | 结果 | 证据 |
| --- | --- | --- |
| Swagger | ✅ | `/swagger-ui.html` 302 → 正常重定向 |
| Dept 分页/等值/时间区间/pageSize | ✅ | total=3 / deptName=技术部 total=1 / createTimeBegin 生效 / pageSize=2 返回 2 行 |
| Dept 创建（全字段） | ✅ | id=4 落库，bit/date/decimal/double 全部正确 |
| Dept 参数校验 | ✅ | 缺 deptName → 20000"部门名称不能为空"；51 字 → 20000"长度不能超过 50" |
| Dept 更新/删除 | ⚠️ | 全字段更新 ✅、删除 ✅（物理删除）；**缺字段更新 → 10000（D5）** |
| 不存在记录 | ⚠️ | 更新 30002 / 删除 30003 正确；**查询 → 10000 系统错误（D6）** |
| Example 创建（枚举+json） | ✅ | userType 出参 `{"code":0,"name":"SYSTEM_USER","desc":"系统用户"}` 对象形态；profile/tags 全链路还原；extra 原样字符串 |
| 枚举入参两种形态 | ✅ | 标量 `0` 与对象 `{"code":0}` 均可（R5 验证通过，无缺陷） |
| 未知枚举 code | ✅ | `99` → 30004"枚举值未匹配: 99" |
| Example PUT 更新 | ✅ | 枚举/json 全量更新成功，DB 存原始 code |
| 强制转换字段 | ✅ | loginCount Integer 正常读写；超出 int 范围 → 10000 兜底，服务不崩溃 |
| Member 逻辑删除链路 | ✅ | 删除 → DB `del_flag=2`；再查为空（走 D6 异常路径）；重复删除 30003；更新已删 30002；分页 total=2（排除已删） |
| 内部表无 Controller | ⚠️ | `/api/v1/job-logs` 返回 **HTTP 200 + 10000** 而非 404（D7） |
| DB 原始值 | ✅ | example 表 `user_type=1 / status='DISABLED' / tags='[{"name": "新标签"}]' / del_flag=0` |

## 5. 缺陷清单

### D1（P1，已确认）Long 枚举生成不可编译代码

- 现象：`codeType: Long` 时生成 `CostTimeEnum`，`mvn package` 编译失败。
- 根因一：枚举模板 `codeLiteral` 未加 `L` 后缀 → `FAST(0, "快速")` 构造参数 `int → Long` 不兼容；
- 根因二：`BaseEnum.fromCode` 只有 Integer / String 两个重载，模板生成的 `BaseEnum.fromCode(Class, Long)` 无匹配方法。
- 证据：`/tmp/aitest-tests/longenums/.../CostTimeEnum.java`，编译错误 4 处。
- 建议：模板对 Long 输出 `0L`；`BaseEnum` 增加 Long 重载。

### D2（P2，已确认）不存在的表先落半成品文件再报错

- 现象：配置 `not_exist_table` 生成时先写入 19 个 `NotExist*` 文件，随后报"读取建表语句失败"退出。
- 证据：`/tmp/aitest-tests/neg-13` 下 19 个文件残留。
- 建议：表级生成前先做存在性校验（fail-fast），不产生半成品。

### D3（P2，已确认）tables 重复配置 example 未报错

- 现象：`tables` 中同时配置 `example` 时，内置示例先生成（21 文件），用户条目静默"跳过"，无任何提示；与设计文档 §5.5"重复配置报错提示"不符。
- 建议：Main 前置检测并报错。

### D4（P2，已确认）列级配置错误信息与文档口径不一致

- 普通列配置 `Map<String,Object>` → 报"不支持 String→Map<String,Object> 转换"，文档要求"集合类型仅用于 json 列"；
- `type: banana` → 报"不支持 Integer→banana 转换"，文档要求"type 不是支持的值"。
- 均能 fail-fast 拦截（功能正确），仅提示文案偏差，建议对齐文档。

### D5（P2，已确认）insert/update 全列直传 + NOT NULL 默认值列缺失 → 10000 系统错误

- 现象：DTO 必填校验只覆盖"NOT NULL 且无默认值"列；但 INSERT/UPDATE SQL 为全列直传，未传 `level`（NOT NULL DEFAULT 0）、`balance` 等列时数据库报 `Column 'level' cannot be null`，被全局兜底成 10000 系统错误。
- 复现：Example 创建缺 level/balance；Dept PUT 缺 level。
- 建议：二选一——（a）DTO 对"有默认值的 NOT NULL"列也生成 @NotNull（配合全量语义）；（b）全局异常处理器将 `DataIntegrityViolationException` 映射为友好业务错误（如 PARAM_INVALID 或新增 30000 段错误码）。

### D6（P2，已确认）查询不存在/已逻辑删除记录返回 10000 系统错误

- 现象：`Assembler.toResponse(null)` 对 null 模型 NPE → 被 Template 兜底为 SYSTEM_ERROR。
- 证据：日志 `NullPointerException ... DeptAssembler.toResponse`。
- 建议：get 链路先判空并抛 `RESOURCE_NOT_FOUND`（30001）。

### D7（P2，已确认）全局异常处理器未按类型分类

- 现象：未映射 URL（内部表 /api/v1/job-logs）、路径尾斜杠等返回 **HTTP 200 + 10000** 而非 404/400/405；`AiTestExceptionHandler` 只处理 Exception 单入口，import 了 `NoResourceFoundException`/`HttpMessageNotReadableException`/`HttpRequestMethodNotSupportedException` 等但未使用（死 import）。
- 与仓库提交历史"404/405/415 分类处理"的既定约定不符。
- 建议：按异常类型分支返回对应错误码/HTTP 状态；清理死 import。

### D8（P3，已确认）Controller 模板残留无用 import

- 生成的 Dept/Example/MemberController 均含 `import java.sql.ParameterMetaData;`（模板遗留，不影响编译）。

## 6. 验证通过的关键点（非缺陷）

- 枚举 `@JsonFormat(OBJECT)` 序列化 + `@JsonCreator` 反序列化：标量 `0` 与对象 `{"code":0}` 两种入参形态均可用（原 R5 风险解除）；
- 枚举未匹配 code → 30004 业务错误，全局兜底正常；
- 逻辑删除全链路（查询过滤/更新失败/重复删除失败/计数排除/表级覆盖全局）符合文档；
- 内部表不生成 web 层文件（CRUD-02 通过）；
- 防覆盖与 force_create 统计口径、报告框输出符合文档；
- 最小配置（v1 形态）行为不变（REGR-01 通过）；
- 更新/删除不存在记录 → 30002/30003（正确路径）。

## 7. 遗留说明

- 冒烟数据：aitest 库当前含测试产生的数据（Dept id=4 已删；example id=1/2；member id=3 del_flag=2），如需干净基线可重跑 `test-configs/aitest-init.sql`；
- 测试配置与脚本均保留在仓库：`test-configs/`（主配置 + 场景配置 + 13 个负向配置 + 建表 SQL）；
- 生成项目保留在 `/Users/jakt/IdeaProjects/aitest`（含修改点：仅数据源 URL 由 token 替换自动指向 aitest，无手工改动）。
