# code-generate-template 测试分析报告（v1 草案，待评审）

> 测试对象：code-generate-template 代码生成器（Java + Freemarker + MySQL）
> 文档依据：[README.md](../README.md)、[generate.yaml.example](../generate.yaml.example)、[generator-column-type-mapping.md](generator-column-type-mapping.md)、skeleton 的 README / AGENTS.md
> 版本基线：git `d994ab2` + 当前工作区未提交改动（v2 设计实施中：骨架瘦身、generateExample、列级转换、逻辑删除、内部表、执行报告）
> 日期：2026-08-10
> 状态：用例设计完毕，待用户评审后执行

---

## 1. 测试概述

本项目是"项目样板 + 表级 CRUD 代码生成器"：通过一个 YAML 配置驱动，完成两件事——

1. **项目初始化**：复制 `skeleton/` 并按 token 规则改名（`AiPlatform` → `${toolPrefix}` 等），产出可编译的多模块 Maven 工程；
2. **表级 CRUD 生成**：连接 MySQL 读取表结构（`information_schema` + `SHOW CREATE TABLE`），按 `tables` 配置渲染 Freemarker 模板，输出 DO / Mapper / Mapper.xml / Model / QueryParam / Repository / Service / Manager / Controller / DTO / Assembler / ParamChecker / SQL 文件。

本次测试的目标是验证生成器**实际行为是否符合文档与工程约定**，重点覆盖 v2 新增能力（列级类型转换、逻辑删除、内部表、示例代码开关、控制台报告），同时回归 v1 既有行为（骨架初始化、默认类型映射、防覆盖跳过）。

## 2. 测试环境

| 项 | 值 |
| --- | --- |
| OS | macOS（arm64） |
| JDK | OpenJDK 17.0.19 |
| Maven | 3.9.16 |
| MySQL | 9.7.1（本机运行，root/123456 可连） |
| 测试库 | 新建 `aitest`（字符集 utf8mb4） |
| 测试输出项目 | 新建同级目录 `/Users/jakt/IdeaProjects/aitest`（生成器产物落点） |
| 生成器构建 | `mvn -q -f generator/pom.xml -DskipTests package` → `generator/target/generator.jar`（已构建通过） |

## 3. 被测功能点清单（需求可测性分析）

### 3.1 配置加载与前置校验（fail-fast）

- 项目命名四件套必填、无默认值：`projectPrefix` / `toolPrefix` / `groupId` / `projectArtifactPrefix`；
- 格式约束：projectPrefix / toolPrefix 驼峰字母数字；projectArtifactPrefix 小写字母数字（无连字符）；
- tables 项必填：`db_table_name` / `model_name`（合法类名）/ `model_comment`；
- jdbc：配置了 tables 或 `generateExample: true` 时必须提供 url / username；
- `logicDelete.enable: true` 时 `column_name` / `normal_value` / `delete_value` 三项必填（全局与表级分别校验）；
- `columns` 列级配置校验：键必须为表内真实列、type 必须为支持值、枚举块完整、code/name 不重复、codeType 支持 Integer/String/Long、转换组合在支持矩阵内、jsonObject+String 非法、集合类型仅限 json 列；
- 原生基础类型（int/long 等）归一化为包装类型并提示；
- 不存在的表：期望在生成前报错且不落半成品文件（**待验证，见 §6 风险 R2**）。

### 3.2 项目骨架初始化

- 复制 skeleton（跳过 target/.git/.idea/out）→ 已存在文件跳过；
- token 替换顺序与结果：`AiplatformApplication`→`${projectPrefix}Application`、`com.jakt.aiplatform`→`${basePackage}`、`AiPlatform`→`${toolPrefix}`、`aiplatform`→`${projectArtifactPrefix}`、`com.jakt`→`${groupId}`；
- Maven 占位符（`${java.version}` 等）原样保留；
- 骨架瘦身：不含任何 User 业务代码；`BaseEnum` / `JsonUtil` / `ENUM_NOT_MATCHED` 等 v2 基础设施就位；
- `skeleton/sql/example.sql` 随项目复制。

### 3.3 示例代码开关 generateExample

- `false`（默认）：不建表、不生成任何示例代码；
- `true`：后台执行 `skeleton/sql/example.sql` 建 `example` 表（含 del_flag、json、decimal、枚举列等 16 列）→ 生成 Tag / Profile POJO → 注入内置示例表配置（两个枚举、login_count 强制转 Integer、tags/profiles json 绑定、强制逻辑删除）→ 走普通表链路产出 19 个文件 + 2 个枚举；
- 重复运行：建表 IF NOT EXISTS 跳过、示例代码已存在整表跳过（进报告）；
- 用户 tables 中重复配置 `example`：文档要求报错提示（**实现未见检测，待验证，见 §6 风险 R3**）。

### 3.4 表级 CRUD 生成

- 标准表输出 19 个文件（DO / Mapper / Mapper.xml / Model / QueryParam / Repository / RepositoryImpl / Convertor / Service / ServiceImpl / Manager / ManagerImpl / ParamChecker / Controller / CreateRequest / UpdateRequest / QueryRequest / Response / Assembler）；
- `generateController: false`（内部表）只生成 12 个数据/业务层文件，web 专属 7 个不生成；
- 防覆盖：DO 已存在或枚举已存在（同名）且非 force → 整表跳过；`force_create: true` → 覆盖全部文件并输出警告；
- 每张表生成 `sql/{db_table_name}.sql` = 真实 `SHOW CREATE TABLE` DDL；已存在默认跳过、force 覆盖；
- BaseDO 强约束：`id` / `create_time` / `update_time` 不生成 DO 字段（继承 BaseDO）；`create_by` / `update_by` / `del_flag` 保留审计列不生成；
- 必填校验：NOT NULL 且无默认值的列 → `@NotBlank/@NotNull` + varchar `@Size(max)`；
- 查询条件全部等值 `=`，含时间区间（createTimeBegin/End、updateTimeBegin/End）。

### 3.5 列级类型映射与转换

- 默认映射矩阵：bigint→Long、int/tinyint→Integer、varchar/char/text→String、datetime→LocalDateTime、date→LocalDate、decimal→BigDecimal、json→String；
- `type: enum`：DO 保持原始 code 类型，Model/DTO 用枚举；生成枚举类（`@JsonFormat(OBJECT)` + `@JsonCreator`，实现 `BaseEnum<T>`）；Convertor 用 `XxxEnum.fromCode` / `ObjectUtil.isNull(...) ? null : getCode()` 互转；
- `type: jsonArray`：Model/DTO = `List<javaObject>`（缺省 `List<Object>`）；Convertor 用 `JsonUtil.parseArray` / `toJson`；
- `type: jsonObject`：Model/DTO = `javaObject`（缺省 `Map<String, Object>`）；Convertor 用 `JsonUtil.parseObject` / `parseMap` / `toJson`；
- `type: <Java类型>`（Integer/Long/BigDecimal/String）：强制转换，Convertor 用 Hutool `Convert.toInt/toLong/toBigDecimal/toStr`；同类型声明直接赋值；
- QueryParam / QueryRequest 保持原始 code 类型（枚举列查询仍用 Integer/String，不使用枚举）；
- 转换代码禁止裸 `Integer.valueOf` / 手写判空三元（枚举 code 反向是唯一例外）。

### 3.6 逻辑删除（SQL 层约定）

- 两级配置：表级 `logicDelete` > 全局 `globalLogicDelete`；
- 生效条件：enable=true 且 `column_name` 在表中真实存在；否则物理删除；
- 行为：selectById / selectPage / selectList / countByQuery / update / updateByCondition 追加 `AND del_flag = 0`；deleteById 变 `UPDATE ... SET del_flag = 删除值 WHERE id = ? AND del_flag = 0`；insert 不写 del_flag（DB 默认值）；
- 字符串列的值自动加引号。

### 3.7 控制台输出与执行报告

- 表级友好消息：成功 / 跳过（原因）/ 强制覆盖（⚠️）/ 示例表带"(示例)"；
- 全部成功不画框：`[gen] 全部 N 张表生成成功`；
- 有跳过或警告画报告框，统计口径：成功 / 跳过 / 警告 + 明细；
- 不输出逐文件路径噪声。

## 4. 测试策略

按"配置校验 → 静态产物 → 可编译性 → 运行期行为"四层递进：

1. **配置层（负向为主）**：构造非法 YAML，验证 fail-fast 报错与提示信息；
2. **产物层（正向为主）**：构造多种表结构与配置，验证生成文件数量、落点、内容（类型、注解、SQL、转换表达式、枚举、token 替换）；
3. **编译层**：对生成项目执行 `mvn -q package`，验证整工程可编译（发现类型/导入/模板 Bug 的最强手段）；
4. **运行层（冒烟）**：启动 bootstrap，通过 HTTP 接口打全链路 CRUD，验证枚举 JSON 形态、逻辑删除、参数校验、异常码；用 SQL 直查验证数据落库与 del_flag 行为。

自动化手段：shell 脚本 + `rg` 产物断言 + Maven 编译 + `curl` 接口断言 + MySQL 直查。人工手段：生成代码走读（对照 README / AGENTS.md 约定）。

## 5. 测试用例设计

> 优先级：P1 = 核心功能，阻断性；P2 = 重要功能/边界；P3 = 规范与体验。
> 编号：CFG 配置校验 / SKEL 骨架 / EXAM 示例 / CRUD 表级生成 / TYPE 类型转换 / LOGIC 逻辑删除 / RUN 编译运行 / OUT 输出报告 / REGR 回归。

### 5.1 测试数据（aitest 库）

| 表 | 用途 | 关键结构 |
| --- | --- | --- |
| `example` | generateExample 内置演示 | 16 列：del_flag、json×3、枚举×2、bigint/int/tinyint/decimal/varchar 等 |
| `sys_job_log` | 内部表 + int 枚举 + 防覆盖 | user_type int（枚举），generateController: false |
| `sys_dept` | 标准 CRUD + 默认类型映射全覆盖 | bigint/int/tinyint/varchar/char/text/datetime/date/decimal/double/bit |
| `member` | 表级逻辑删除 + force_create | del_flag tinyint，logicDelete 表级覆盖 |
| （不存在表） | 负向：不存在的表名 | — |

### 5.2 配置校验（CFG）

| 编号 | 优先级 | 用例 | 步骤 | 预期结果 |
| --- | --- | --- | --- | --- |
| CFG-01 | P1 | 缺少 projectPrefix | 配置缺 projectPrefix 运行 | 报错列出缺失项，退出码非 0 |
| CFG-02 | P1 | projectPrefix 非法 | 值含下划线/数字开头 | 报错"projectPrefix 需为驼峰字母/数字" |
| CFG-03 | P1 | toolPrefix 非法 | 值含非法字符 | 同上，提示 toolPrefix |
| CFG-04 | P1 | 缺少 groupId / projectArtifactPrefix | 分别缺省 | 报错列出缺失项 |
| CFG-05 | P1 | projectArtifactPrefix 含连字符/大写 | 值 ai-prod / AiProd | 报错，提示 Java 包名约束 |
| CFG-06 | P1 | 配置 tables 缺 jdbc | tables 非空、无 jdbc 块 | 报错"配置了 tables ... 缺少 jdbc.url / jdbc.username" |
| CFG-07 | P1 | generateExample:true 缺 jdbc | 无 jdbc 块 | 报错提示建表与读表结构需要 jdbc |
| CFG-08 | P1 | 空配置（无 tables、无 example） | 只有命名 + outputDir | 骨架初始化成功，提示"未配置 tables，跳过表级生成" |
| CFG-09 | P1 | tables 项缺必填三件套 | 分别缺 db_table_name / model_name / model_comment | 报错定位到具体表 |
| CFG-10 | P1 | model_name 非法类名 | 值 user-name / 1user | 报错"model_name 需为合法 Java 类名" |
| CFG-11 | P1 | 逻辑删除缺三件套 | 全局/表级 enable:true 但缺 column_name / normal_value / delete_value | 报错定位配置名 |
| CFG-12 | P1 | columns 键不是表内真实列 | 配置列 xx 不在表中 | 报错"列 xx 不存在于表 yy" |
| CFG-13 | P1 | columns 缺 type | 配置列但无 type | 报错"配置了 columns 但缺少 type" |
| CFG-14 | P1 | type 非法值 | type: banana | 报错"type 不是支持的值" |
| CFG-15 | P2 | 原生类型归一化 | type: int（bigint 列） | 不报错，日志提示；Model 为 Integer |
| CFG-16 | P1 | enum 块校验 | 缺 enum 块 / 缺 className / className 非法 | 分别报错 |
| CFG-17 | P1 | codeType 非法 | codeType: Double | 报错"只支持 Integer/String/Long" |
| CFG-18 | P1 | 枚举 value 缺 code/name/desc | 任一缺失 | 报错 |
| CFG-19 | P1 | 枚举 code / name 重复 | 两个 value 同 code 或同 name | 报错"code 重复" / "name 重复" |
| CFG-20 | P1 | codeType=Integer 但 code 非数字 | code: abc | 报错"无法解析" |
| CFG-21 | P1 | 普通列配置集合类型 | type: Map<String,Object>（非 json 列） | 期望报错"集合类型仅用于 json 列"（**验证实际提示，见 R4**） |
| CFG-22 | P1 | jsonObject + javaObject:String | type: jsonObject, javaObject: String | 报错提示改用 type: json |
| CFG-23 | P2 | jsonArray/jsonObject 用于非 json 列 | 在 varchar 列配置 | 报错"只适用于 json 类型列" |
| CFG-24 | P2 | javaObject 非法 | 含非法字符 | 报错 |
| CFG-25 | P2 | 不支持的转换组合 | bigint→LocalDateTime | 报错"不支持 ... 转换" |
| CFG-26 | P1 | 配置不存在的表 | db_table_name 不存在 | 期望生成前 fail-fast、不落任何半成品文件（**验证 R2**） |

### 5.3 骨架初始化（SKEL）

| 编号 | 优先级 | 用例 | 步骤 | 预期结果 |
| --- | --- | --- | --- | --- |
| SKEL-01 | P1 | 首次生成骨架 | 最小配置跑生成 | 目录结构完整：bootstrap / web / biz/service-impl / core/{model,repository,service} / common/{dal,util,integration} + 各 pom |
| SKEL-02 | P1 | token 替换正确性 | 生成后全仓 grep | `AiplatformApplication`→`AiProdApplication`；`com.jakt.aiplatform`→`com.jakt.aiprod`；`AiPlatformException/AiPlatformInvoker/AiPlatformTemplate`→`AiProd*`；`aiplatform`→`aiprod`；无残留 `com.jakt.aiplatform` / `AiPlatform` / `aiplatform` 业务引用 |
| SKEL-03 | P1 | Maven 占位符保留 | 检查各 pom | `${java.version}` 等原样保留，未被 token 替换破坏 |
| SKEL-04 | P1 | 骨架瘦身 | grep User 业务类 | 无 UserDO / UserMapper / UserController / UserQueryParam 等任何 User 业务代码 |
| SKEL-05 | P1 | 重复初始化跳过 | 第二次运行同一 outputDir | 输出"生成 0 个文件，跳过 N 个"；已存在文件不被覆盖 |
| SKEL-06 | P1 | example.sql 复制 | 检查目标 sql/ | `sql/example.sql` 存在且内容为示例表 DDL |
| SKEL-07 | P2 | outputDir 不存在自动创建 | 输出目录不存在 | 自动创建并生成成功 |

### 5.4 generateExample（EXAM）

| 编号 | 优先级 | 用例 | 步骤 | 预期结果 |
| --- | --- | --- | --- | --- |
| EXAM-01 | P1 | 默认关闭 | generateExample 缺省或 false | 不建 example 表；目标项目无 Example/Tag/Profile/UserTypeEnum |
| EXAM-02 | P1 | 开启建表 | true + jdbc | example 表创建成功，16 列结构符合 skeleton/sql/example.sql |
| EXAM-03 | P1 | 示例代码产物 | true 跑全功能配置 | Tag/Profile + UserTypeEnum/UserStatusEnum + Example 19 文件（合计 21 个 + SQL），落点正确 |
| EXAM-04 | P1 | 重复运行 | 第二次运行 true | 建表跳过；示例代码整表跳过，进入执行报告（跳过原因"DO 已存在"） |
| EXAM-05 | P2 | tables 重复配置 example | 用户 tables 加 example 表 | 文档要求报错提示（**验证 R3：实现未检测，可能静默处理）** |
| EXAM-06 | P2 | 建表无写权限 | 用只读账号 | fail-fast 报错退出，提示检查数据库写权限 |

### 5.5 表级 CRUD 生成（CRUD）

| 编号 | 优先级 | 用例 | 步骤 | 预期结果 |
| --- | --- | --- | --- | --- |
| CRUD-01 | P1 | 标准表 19 文件 | 默认 generateController:true | 19 个文件全部生成，路径与 TEMPLATES 映射一致 |
| CRUD-02 | P1 | 内部表 12 文件 | generateController:false | 无 Controller/Create/Update/QueryRequest/Response/Assembler/ParamChecker；DO/Mapper/XML/Model/QueryParam/Repository/Impl/Convertor/Service/Impl/Manager/Impl 齐全 |
| CRUD-03 | P1 | DO 已存在跳过 | 首次生成后再次运行（非 force） | 整表跳过，提示"DO 已存在；如需覆盖请配置 force_create: true"；SQL 不覆盖 |
| CRUD-04 | P1 | force_create 覆盖 | 修改表结构后 force 重跑 | 19 文件 + SQL 全部覆盖为最新结构；输出 ⚠️ 警告 |
| CRUD-05 | P1 | 枚举冲突处理 | 手动放一个同名枚举再生成（非 force / force 各一次） | 非 force：整表跳过（原因"枚举 XxxEnum 已存在"）；force：覆盖枚举并警告 |
| CRUD-06 | P1 | SQL 文件生成 | 检查 sql/ | 每张配置的表有 `sql/{db_table_name}.sql`，内容含真实建表 DDL，注释头正确 |
| CRUD-07 | P1 | SQL 防覆盖 | 手动改 sql 后重跑 | 默认跳过（内容不变）；force 覆盖 |
| CRUD-08 | P2 | 产物与表结构对齐 | 走读 sys_dept 产物 | 字段名/类型/注释/必填注解与建表语句一致 |
| CRUD-09 | P2 | 基类强约束 | 走读 DO/Model | id/create_time/update_time 不入 DO；create_by/update_by/del_flag 不生成任何字段 |
| CRUD-10 | P1 | 必填校验注解 | 对 example 表 | user_name/password 有 @NotBlank+@Size；nick_name/age 等可空列无 @NotNull；balance 无 @Size（非字符串） |

### 5.6 类型映射与列级转换（TYPE）

| 编号 | 优先级 | 用例 | 步骤 | 预期结果 |
| --- | --- | --- | --- | --- |
| TYPE-01 | P1 | 默认映射矩阵 | 建 sys_dept 全类型表生成 | bigint→Long、int/tinyint→Integer、varchar/char/text→String、datetime→LocalDateTime、date→LocalDate、decimal→BigDecimal、double→Double、bit→Boolean、json→String |
| TYPE-02 | P1 | 枚举列分层类型 | 走读 example 产物 | DO: `Integer userType` / `String status`；Model/DTO: `UserTypeEnum` / `UserStatusEnum`；Convertor: `UserTypeEnum.fromCode(source.getUserType())`、`ObjectUtil.isNull(...) ? null : ...getCode()` |
| TYPE-03 | P1 | 枚举模板 | 走读生成枚举 | implements BaseEnum<Integer/String>；@JsonFormat(OBJECT)；@JsonCreator；name=常量名；desc 进 javadoc/getDesc；未匹配走 BaseEnum.fromCode 抛 ENUM_NOT_MATCHED |
| TYPE-04 | P1 | 枚举序列化形态 | 启动后接口返回含枚举 | JSON 为对象 `{"code":0,"name":"SYSTEM_USER","desc":"系统用户"}`，非字符串非 @ENUM 形式 |
| TYPE-05 | P1 | 枚举反序列化形态 | 创建接口入参传 `0` 标量与 `{"code":0}` 对象各一次 | 两者行为锁定（文档 §7.3 要求冒烟确认；**若对象形态失败，按缺陷记录**，见 R5） |
| TYPE-06 | P2 | 未匹配 code | 入参/DB 放 code=99 | 返回/抛 ENUM_NOT_MATCHED(30004)，业务异常被全局处理器兜底 |
| TYPE-07 | P1 | jsonArray 转换 | 走读 example Convertor | Model=`List<Tag>`；`JsonUtil.parseArray(source.getTags(), Tag.class)` / `JsonUtil.toJson(...)`；DO 保持 String |
| TYPE-08 | P1 | jsonObject 转换 | 走读 example Convertor | Model=`Profile`；`JsonUtil.parseObject(source.getProfile(), Profile.class)` / `toJson` |
| TYPE-09 | P2 | 缺省 javaObject | 配 jsonArray/jsonObject 不写 javaObject | `List<Object>` / `Map<String, Object>`，Convertor 用 parseArray(x)/parseMap(x) |
| TYPE-10 | P2 | 泛型 javaObject | javaObject 为 `Map<String,Object>` 等 | 生成 `new TypeReference<...>() {}`，导入 TypeReference |
| TYPE-11 | P1 | 强制类型转换 | example login_count | DO `Long`，Model/DTO `Integer`；Convertor `Convert.toInt(...)` / `Convert.toLong(...)` |
| TYPE-12 | P1 | 同类型声明 | type:String 配 varchar 列 | modelType=String、conversion NONE、直接赋值 |
| TYPE-13 | P1 | Query 保持原始类型 | 走读 QueryParam/QueryRequest | 枚举列查询字段为 Integer/String，不是枚举 |
| TYPE-14 | P1 | codeType:Long 枚举 | 配 Long 枚举列生成并编译 | **预判缺陷 R1：BaseEnum 只有 Integer/String 两个 fromCode 重载，Long 枚举产物编译失败**；若通过则验证 fromCodeJson Long 反查 |
| TYPE-15 | P2 | json 列默认映射 | example extra 列 | Model/DTO 为 String，无 JsonUtil 转换（NONE 直传） |
| TYPE-16 | P2 | 转换代码规范 | grep 生成 Convertor | 无裸 `Integer.valueOf` / 手写 `== null ?`（枚举反向除外）；全部走 Hutool/JsonUtil |

### 5.7 逻辑删除（LOGIC）

| 编号 | 优先级 | 用例 | 步骤 | 预期结果 |
| --- | --- | --- | --- | --- |
| LOGIC-01 | P1 | 全局逻辑删除 SQL | globalLogicDelete 配 del_flag 0/1 生成 | Mapper.xml：selectById/update 带 `AND del_flag = 0`；queryConditions 带 `AND del_flag = 0`；deleteById 为 `UPDATE ... SET del_flag = 1 WHERE id = #{id} AND del_flag = 0` |
| LOGIC-02 | P1 | 表级覆盖全局 | 全局 A 配置 + 表级 B 配置 | 该表按 B 生成，其余表按 A |
| LOGIC-03 | P2 | 配置列不存在 | enable:true 但表无 del_flag | 不报错，退化物理删除（DELETE） |
| LOGIC-04 | P2 | 字符串列值引号 | 逻辑删除列 varchar | SQL 中值为 `'0'` / `'1'`（带引号）；int 列不加引号 |
| LOGIC-05 | P1 | 运行期行为 | 全链路 | 删除后 selectById/page/list/count 查不到；更新已删记录 → UPDATE_FAILED；重复删除 → DELETE_FAILED；updateByCondition 同样带 del_flag 条件 |
| LOGIC-06 | P2 | insert 不写 del_flag | 直查 SQL/数据 | insertColumns 不含 del_flag；落库后 del_flag 为 DB 默认 0 |
| LOGIC-07 | P2 | del_flag 不生成字段 | 走读产物 | DO/Model/Mapper 列清单（selectColumns/insertColumns/updateSet）均不含 del_flag |

### 5.8 编译与运行冒烟（RUN）

| 编号 | 优先级 | 用例 | 步骤 | 预期结果 |
| --- | --- | --- | --- | --- |
| RUN-01 | P1 | 全功能产物编译 | 生成项目 `mvn -q package` | 全模块编译打包通过（example + 枚举 + json + 强制转换 + 逻辑删除 + 内部表组合） |
| RUN-02 | P1 | 业务表产物编译 | sys_dept/member 等 | 编译通过 |
| RUN-03 | P1 | 启动 | 运行 bootstrap | 启动成功，Swagger 可访问 |
| RUN-04 | P1 | 接口全链路 | POST 创建 → GET 查询 → PUT 更新 → DELETE 删除 → 再 GET | 各步成功，数据落库；删除后再查为资源不存在/空 |
| RUN-05 | P1 | 分页与时间区间 | page?pageNum/pageSize/createTimeBegin/End | 分页总数与列表正确，LIMIT/时间过滤生效 |
| RUN-06 | P1 | 参数校验 | 缺必填字段、varchar 超长 | PARAM_INVALID(20000)，错误信息含字段提示 |
| RUN-07 | P2 | 查询不存在 ID | GET /{不存在id} | 期望友好错误（**预判 R6：当前 Assembler.toResponse(null) 可能 NPE → SYSTEM_ERROR**） |
| RUN-08 | P2 | 更新/删除不存在 ID | PUT/DELETE 不存在 id | UPDATE_FAILED / DELETE_FAILED |
| RUN-09 | P2 | 枚举字段链路 | 创建带 user_type/status → 查询返回 | 入参收 code、出参为 JSON 对象形态；DB 存原始 code |
| RUN-10 | P2 | json 字段链路 | 创建带 tags/profile → 查询返回 | 全链路 List<Tag>/Profile 序列化/反序列化正确；extra 原样字符串 |
| RUN-11 | P2 | 强制转换边界 | login_count 大数（超 int） | 文档标注溢出风险由配置人负责；验证不导致服务崩溃、异常被兜底 |
| RUN-12 | P2 | web 层规范 | 走读 Controller | 统一走 Template.execute/executeWithoutResult；返回 AiProdResult；无业务逻辑；**P3：检查是否残留 `java.sql.ParameterMetaData` 无用 import（见 R7）** |

### 5.9 控制台输出与报告（OUT）

| 编号 | 优先级 | 用例 | 步骤 | 预期结果 |
| --- | --- | --- | --- | --- |
| OUT-01 | P1 | 全成功无框 | 首次全功能生成 | 无报告框，输出 `[gen] 全部 N 张表生成成功` |
| OUT-02 | P1 | 有跳过/警告画框 | 重复运行 + force 混合 | 画执行报告框；成功/跳过/警告计数与明细正确 |
| OUT-03 | P1 | 表级消息 | 混合场景 | 成功/跳过（原因）/⚠️ 覆盖/`example(示例)` 标记正确 |
| OUT-04 | P2 | 无路径噪声 | 观察输出 | 不打印逐文件绝对路径；force 时提示覆盖风险 |
| OUT-05 | P2 | 异常退出信息 | 各负向配置 | 错误信息清晰、退出码非 0 |

### 5.10 回归兼容（REGR）

| 编号 | 优先级 | 用例 | 步骤 | 预期结果 |
| --- | --- | --- | --- | --- |
| REGR-01 | P1 | v1 最小配置行为不变 | 只写命名 + jdbc + tables 三件套 | 生成成功；默认映射/19 文件/跳过逻辑与 v1 一致；无枚举/json/逻辑删除副作用 |
| REGR-02 | P1 | 骨架零残留 | grep | 全仓无 User 业务类引用（含 README/AGENTS 示例说明按新约定） |
| REGR-03 | P2 | 无 tables 场景 | generateExample:false + 空 tables | 仅骨架文件，无任何表级产物与 sql 目录业务文件 |

## 6. 代码走读发现的风险与待确认点（预判缺陷）

> 以下为测试前走读代码发现的疑点，将作为测试用例的验证重点；确认后按缺陷等级（P1/P2/P3）记入缺陷清单。

| 编号 | 级别 | 描述 | 依据 |
| --- | --- | --- | --- |
| R1 | P1 | **BaseEnum 缺少 Long 重载**：`BaseEnum.fromCode` 只有 Integer / String 两个静态重载，但枚举模板对 `codeType: Long` 生成 `BaseEnum.fromCode(Long)` / `BaseEnum.fromCode(Class, Long.valueOf(code))`，泛型上界不匹配 → **Long 枚举产物编译失败**。设计文档 §7.1 的 BaseEnum 同样只有两个重载，属设计缺口带入实现。 | [BaseEnum.java](../../../code-generate-template/skeleton/core/model/src/main/java/com/jakt/aiplatform/core/model/enums/BaseEnum.java)、`{EnumName}.java.ftl` |
| R2 | P2 | **不存在的表产生半成品**：`DbMetaReader.read` 对不存在的表返回空列元数据（不报错），19 个文件先写完，随后 `SHOW CREATE TABLE` 才抛异常 → 已落文件无法回滚。期望生成前校验表存在性（fail-fast，不落半成品）。 | `DbMetaReader.readCreateTable`、`CrudGenerator.run` 顺序 |
| R3 | P2 | **tables 重复配置 example 无检测**：文档 §5.5 要求"用户同时配置 example 表时以内置示例配置为准，重复配置报错提示"；实现中 `Main` 只注入内置配置，未检测用户 tables 中的 `example`，实际行为是内置先生成、用户条目再"跳过"，静默处理。 | `Main.main`、`ExampleGenerator` |
| R4 | P2 | **集合类型错误信息偏差**：文档 §6.4 要求普通列配置 `Map/Set/List` 报"集合类型仅用于 json 列"；`GeneratorConfig.validateColumnConfig` 未做该检查，实际落入 `applyCoerceConfig` 报"不支持 X→Y 转换"。功能上仍报错，信息不符合文档口径。 | `GeneratorConfig.validateColumnConfig` |
| R5 | P1 | **枚举 JSON 序列化/反序列化形态未锁定**：枚举模板 `@JsonCreator fromCodeJson(String code)` 用 String 参数 + `Integer.valueOf`，与文档"`@JsonCreator fromCode(code)` 接收标量"的实现不一致。Jackson 对 `@JsonFormat(OBJECT)` + delegating creator 的组合，`{"code":0}` 对象入参可能反序列化失败。文档自身标注"实施时用冒烟测试锁定"，RUN-04/TYPE-05 覆盖。 | `{EnumName}.java.ftl`、设计文档 §7.3 |
| R6 | P2 | **查询不存在记录可能返回 SYSTEM_ERROR 而非友好错误**：`get` 链路 `Assembler.toResponse(null)` 会 NPE，被 Template 兜底为 SYSTEM_ERROR；预期应返回 RESOURCE_NOT_FOUND 或明确提示。 | `{Class}Assembler.java.ftl`、`AiPlatformTemplate` |
| R7 | P3 | **Controller 模板残留无用 import**：`import java.sql.ParameterMetaData;` 与 Controller 无关，属模板冗余（不影响编译，违反整洁约定）。 | `{Class}Controller.java.ftl` |
| R8 | P2 | **枚举 codeType 缺省推断**：varchar 列不写 codeType → 推断 String ✓；但若 varchar 列配枚举而 code 想用数字（`"123"`），生成 String 枚举，需配置人显式指定 codeType: Integer；文档一致，仅提示验证。 | `DbMetaReader.applyEnumConfig` |

## 7. 执行计划（评审通过后）

1. 创建测试数据库：`CREATE DATABASE aitest DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;`，建测试表（sys_dept / sys_job_log / member）并灌入样例数据；
2. 在同级目录创建测试项目：配置 `outputDir: /Users/jakt/IdeaProjects/aitest`，按 5 个场景 YAML 依次执行生成器（最小回归 / 全功能 / 内部表 / 重复运行+force / 负向校验批量）；
3. 产物静态断言（文件数量、落点、grep 内容）、`mvn -q package` 编译验证；
4. 启动 bootstrap，执行 HTTP 冒烟用例（CRUD 全链路、枚举形态、逻辑删除、校验、分页、json 字段）；
5. 汇总执行结果，输出《测试执行报告 + 缺陷清单》（含 R1–R8 验证结论），与测试分析报告一起归档到 aitest 项目；
6. 缺陷按优先级推动修复（P1 阻断：R1/R5 相关），修复后回归。

## 8. 交付物清单

- 本报告（测试分析 + 用例设计）：`docs/test-analysis-report.md`
- 待执行后产出：`docs/test-execution-report.md`（执行结果 + 缺陷清单，归档到 aitest 项目）
