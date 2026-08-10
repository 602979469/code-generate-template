# 生成器列级类型映射与表级配置设计文档（v2）

> 状态：设计已评审，待实施
> 范围：code-generate-template 生成器的 `generate.yaml` 扩展 + 模板改造 + 控制台输出
> 日期：2026-08-10

## 1. 背景

当前生成器只从数据库 `information_schema` 读取列信息，按"数据库类型 → Java 类型"固定表做默认映射（`bigint→Long`、`varchar→String`…），存在两个缺口：

1. **语义信息缺失**：数据库无法表达"这个 int 列是枚举"、"这个 json 列是数组"、"这个 varchar 列是枚举 code"。DO → Model 的转换（`Convertor`）只能同类型透传，Model 无法展示更有表达力的类型。
2. **无法强制指定类型**：用户想把 `bigint` 映射成 `int`、把 `varchar` 映射成 `int`，没有告知入口。

同时，现有生成器缺少三类通用能力：**内部表不暴露 Controller**、**逻辑删除字段**、**面向人的控制台输出**。

此外，骨架目前内置了一套 User 示例业务代码（DO / Mapper / Model / Service / Controller…），新项目一复制就带着一堆"别人家的业务代码"，sql 目录却是空的。本设计把骨架瘦身为纯基础设施，业务示例改为由 `generateExample` 开关按需生成，示例表定名为 `example`（避免 `user` 这类易重名的表名）。

本设计为 `generate.yaml` 增加列级配置、表级配置与示例代码开关，配套新增枚举基座、生成枚举、Convertor 转换逻辑、逻辑删除 SQL 与执行报告。

## 2. 目标与非目标

### 目标

- `example` 表演示字段扩展，覆盖常用数据类型：`json`、`jsonArray`、枚举（int code / string code）、`decimal`、可空/非空 `varchar`、`int`、`bigint`、`tinyint`，并**强制演示逻辑删除**（增删改查全链路）。
- `generate.yaml` 列级配置：枚举列自动生成枚举到 `core-model/enums`；`bigint → int` 等强制转换；`json` / `jsonArray` 语义。
- 枚举基座 `BaseEnum<T>`：`T` 为 code 类型，`name` 为枚举常量名；未匹配 code **抛异常**（新异常码"枚举值未匹配"）。
- 枚举序列化：前端拿到的是 **JSON 对象**（`{"code":0,"name":"SYSTEM_USER","desc":"系统用户"}`），不是 `@ENUMxxxx`。
- 表级配置：`generateController: false` 内部表不生成 Controller；`globalLogicDelete` / `logicDelete` 两级逻辑删除（表级优先）。
- 骨架瘦身：删除 skeleton 内全部 User 业务代码，只保留基础设施；`skeleton/sql/example.sql` 内置示例表 DDL。
- 示例开关：`generateExample: true` 时后台创建 `example` 表并生成完整示例模块（含枚举、jsonArray、类型转换、逻辑删除演示）；false 时不生成任何示例代码。已创建/已生成则跳过，无强制覆盖选项。
- 类型转换一律使用三方工具（Hutool `Convert` / `NumberUtil` / `StrUtil` 等），禁止手写判空与类型判断。
- 控制台输出：每表一条友好消息（成功/跳过/强制覆盖），结束时输出执行报告框（成功/跳过/警告及原因）。
- 不配置新选项时，行为与现状完全一致（向后兼容）。

### 非目标（本期不做）

- LIKE / 范围等复杂查询条件。
- `type: json` 保持原始字符串不做解析；对象/数组解析走 `type: jsonObject` / `type: jsonArray` + `javaObject`。
- 枚举国际化、字典接口。
- 枚举列作为查询条件（查询一律按原始 code 类型，原因见 §11）。
- MySQL 原生 `enum` 列自动解析（本期按 String 处理，可手动配置枚举；原生 enum 用得少，见 §15）。

## 3. 设计原则

- **接口小而深**：列级配置只暴露 `type` / `enum` / `javaObject` 少数几个键，背后驱动 DO、Model、Convertor、DTO、枚举、Assembler 一整条链路的改动。
- **转换逻辑收敛在一处**：DO ↔ Model 的类型转换只发生在仓储 `Convertor`；模板是转换规则的唯一实现点，"改一次，全项目生效"。
- **DO 永不漂移**：`DO` 与 `Mapper.xml` 始终使用数据库默认映射的原始类型（枚举列 `user_type → Integer`、json → `String`），保证 SQL / ResultSet / MyBatis 行为不变；类型变化只发生在 Model 及以上层。
- **表级独立、逐表生成**：每张表独立判定成功/跳过/覆盖，不做跨表联动，判断逻辑保持简单。

## 4. example 表演示字段（目标 DDL）

示例表定名 `example`，类名 `Example`（`ExampleDO` / `ExampleMapper` / `Example` / `ExampleQueryParam` …）。字段覆盖常用数据类型并包含逻辑删除列：

```sql
CREATE TABLE `example` (
  `id`          bigint        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `create_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag`    tinyint       NOT NULL DEFAULT 0 COMMENT '删除标志(0正常 1已删除)',  -- 逻辑删除列

  `user_name`   varchar(30)   NOT NULL COMMENT '用户名',                 -- varchar 非空
  `password`    varchar(100)  NOT NULL COMMENT '密码',                   -- varchar 非空
  `nick_name`   varchar(50)   DEFAULT NULL COMMENT '昵称',               -- varchar 可空
  `age`         int           DEFAULT NULL COMMENT '年龄',               -- int 可空
  `level`       tinyint       NOT NULL DEFAULT 0 COMMENT '等级',         -- tinyint
  `login_count` bigint        NOT NULL DEFAULT 0 COMMENT '登录次数',     -- bigint（演示强制转 int）
  `balance`     decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '余额',      -- decimal → BigDecimal
  `user_type`   int           NOT NULL DEFAULT 0 COMMENT '用户类型(0系统用户 1普通用户)',  -- int 枚举
  `status`      varchar(20)   NOT NULL DEFAULT 'ENABLED' COMMENT '状态(ENABLED/DISABLED)', -- varchar 枚举
  `profile`     json          DEFAULT NULL COMMENT '扩展信息(json 对象)',  -- json
  `tags`        json          DEFAULT NULL COMMENT '标签(json 数组)',      -- jsonArray
  `extra`       json          DEFAULT NULL COMMENT '扩展原始JSON(默认映射String)', -- json（不配置 type）

  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='示例表';
```

> `tags` 在 MySQL 中仍是 `json` 列，语义上是数组，通过列配置 `type: jsonArray` 告知生成器。

## 5. generate.yaml 配置设计

### 5.1 全局配置（新增）

**以下均为可选配置：配置文件里不写，默认就不开启，行为与现状完全一致。各功能示例只展示本段知识点，不掺入无关配置项。**

| 配置项 | 缺省行为（不写） | 开启方式 |
| --- | --- | --- |
| `generateExample` | 不建表、不生成任何示例代码 | 写 `generateExample: true` |
| `globalLogicDelete` | 所有表物理删除（配置见 §8） | 写 `globalLogicDelete: { enable: true, ... }` |
| `logicDelete`（表级） | 继承全局；全局未配则物理删除（配置见 §8） | 表内写 `logicDelete: { enable: true, ... }` |

全局配置示例：

```yaml
# 可选：是否生成示例代码（默认 false）；true 时后台创建 example 表并生成完整示例模块
generateExample: false
```

### 5.2 表级配置（新增项）

```yaml
tables:
  - db_table_name: xxx       # 必填
    model_name: Xxx          # 必填
    model_comment: xxx       # 必填
    # force_create / generateController / logicDelete / columns 均为可选，见对应章节
```

> 日常使用只写三个必填项即可；`force_create`（§5.4 之后不再展开，沿用现有语义）、`generateController`（§9）、`logicDelete`（§8）、`columns`（§5.3）按需添加。

### 5.3 列级配置

**列级转换逻辑必须通过 `type` 显式声明**：生成器不会猜测某列是不是枚举、是不是 JSON 数组——配置了 `type` 才做对应转换，没配 `type` 一律走数据库默认映射（§6.1）。

分两类介绍：**① 枚举转换**、**② JSON 与强制类型转换**。未配置的列不需要出现在 `columns` 里。

#### ① 枚举转换（type: enum）

```yaml
columns:
  user_type:                          # 数据库列名（键）
    type: enum                        # 显式声明：int 列映射为枚举
    enum:
      className: UserTypeEnum
      codeType: Integer
      values:
        - code: 0
          name: SYSTEM_USER
          desc: 系统用户
        - code: 1
          name: NORMAL_USER
          desc: 普通用户
  status:
    type: enum                        # 显式声明：varchar 列映射为枚举
    enum:
      className: UserStatusEnum
      codeType: String
      values:
        - code: ENABLED
          name: ENABLED
          desc: 启用
        - code: DISABLED
          name: DISABLED
          desc: 停用
```

生成后：DO 保持 `Integer userType` / `String status`，Model 变成 `UserTypeEnum userType` / `UserStatusEnum status`，Convertor 自动 `UserTypeEnum.fromCode(...)` 互转（详见 §6.3 / §7）。

#### ② JSON 与强制类型转换（type: jsonArray / jsonObject / Java 类型）

```yaml
columns:
  tags:
    type: jsonArray                   # 显式声明：JSON 数组
    javaObject: com.jakt.aiplatform.core.model.domain.Tag      # 元素类型 → List<Tag>
  profile:
    type: jsonObject                  # 显式声明：JSON 对象
    javaObject: com.jakt.aiplatform.core.model.domain.Profile # 目标类型 → Profile
  login_count:
    type: Integer                     # 显式声明：bigint 列强制转 int（风险自负）
```

生成后：`tags` 为 `List<Tag>`（不写 `javaObject` 默认 `List<Object>`），`profile` 为 `Profile`（不写默认 `Map<String, Object>`），`login_count` 为 `Integer`，Convertor 用 `JsonUtil` / Hutool `Convert` 自动转换（详见 §6.3）。

> `extra` 列不配置 `type`，走 json → String 默认映射，无需写进 `columns`。

| 键 | 必填 | 取值 | 作用 |
| --- | --- | --- | --- |
| `type` | 否 | `enum` / `json` / `jsonArray` / `jsonObject` / 任意 Java 类型（如 `Integer`、`Long`、`List<...>`） | **显式声明转换逻辑**：enum=枚举；json=原始字符串（String）；jsonArray=数组；jsonObject=对象；Java 类型=强制类型转换（风险自负）。不写 = 数据库默认映射 |
| `javaObject` | 否（配 `type: jsonArray` / `jsonObject` 时可用） | 全限定类名，如 `com.jakt.aiplatform.core.model.domain.Tag` | jsonArray 元素类型 / jsonObject 目标类型；不配则 `List<Object>` / `Map<String, Object>` |
| `enum.className` | 是（配 `type: enum` 时） | 合法 Java 类名 | 生成到 `core-model/enums` 包的枚举类名（对应你示例中的 `name`） |
| `enum.codeType` | 否 | `Integer` / `String` / `Long` | 枚举 code 类型；缺省按数据库列推断（int 族→Integer、varchar/char→String、bigint→Long） |
| `enum.values[].code` | 是 | 与 codeType 匹配 | 数据库存储值，不能重复 |
| `enum.values[].name` | 是 | 合法 Java 标识符（建议大写蛇形） | 枚举常量名（如 `SYSTEM_USER`），不能重复 |
| `enum.values[].desc` | 是 | 字符串 | 枚举描述，进入 javadoc 与 `getDesc()` |

**类型支持规则**

- **原生基础类型自动归一化**：`type: int` / `long` / `boolean` 等会自动归一化为包装类型（`Integer` / `Long` / `Boolean`），不报错、生成日志提示；Model 字段允许 null、Hutool `Convert` 返回包装类型，禁止直接使用原生类型。
- **集合类型只对 json 列有意义**：`Map` / `Set` / `List`（含泛型如 `Map<String, Object>`、`Set<String>`）只能出现在 `type: jsonObject` / `jsonArray` 的 `javaObject` 上；普通列配置集合类型直接报错。
- **`type: jsonObject` + `javaObject: String` 非法**：JSON 对象不能解析成 String，校验报错并提示改用 `type: json`；`type: jsonArray` + `javaObject: String` 合法（→ `List<String>`，数组元素可以是基本类型）。

### 5.4 完整示例（日常最简形态）

```yaml
generateExample: true          # 生成示例代码，并在后台创建 example 表

projectPrefix: AiPlatform
toolPrefix: AiPlatform
groupId: com.jakt
projectArtifactPrefix: aiplatform
jdbc:
  url: jdbc:mysql://localhost:3306/aiplatform?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
  username: root
  password: "123456"
outputDir: /Users/jakt/IdeaProjects/AiProd

tables:
  - db_table_name: sys_job_log
    model_name: JobLog
    model_comment: 任务日志
```

> 这是日常最简形态：只写必填项。`example` 示例表不再出现在 `tables` 里，由 `generateExample: true` 生效（见 §5.5）。内部表、逻辑删除、列级转换等按需在对应章节追加配置。

### 5.5 示例代码与骨架改造（generateExample）

#### 骨架瘦身

- **删除 skeleton 内全部 User 业务代码**（DO / Mapper / Mapper.xml / Model / QueryParam / Repository / RepositoryImpl / Convertor / Service / ServiceImpl / Manager / ManagerImpl / Controller / web DTO / Assembler / ParamChecker / Response），骨架只保留基础设施：
  - common-dal：`BaseDO`、pom；
  - core-model：`BaseModel`、`BaseEnum`（新增）、`ErrorCodeEnum`（含 `ENUM_NOT_MATCHED`）、`AiPlatformException`、`ErrorCode`、`Result` / `PageResult`、`BizTemplate`、常量、工具类；
  - common-util / common-integration / core-repository / core-service / biz-service-impl / web / bootstrap：全部为通用设施，无业务类。
- **sql 目录**：`skeleton/sql/example.sql` 内置示例表 DDL（§4 完整版），随项目初始化复制到目标项目 `sql/example.sql`。
- skeleton 的 README / AGENTS.md 同步改写：不再以 User 模块讲解分层，改为"示例代码由 `generateExample` 生成"。

#### generateExample 生成流程

`generateExample: true` 时，生成器按固定顺序执行：

1. **后台建表**：连接 jdbc，执行 `skeleton/sql/example.sql`（`CREATE TABLE IF NOT EXISTS`），确保 `example` 表存在且结构满足 §4；已存在则跳过（无强制重建）；
2. **读取元数据**：走标准 DbMetaReader 读表结构；
3. **按内置列配置生成示例模块**：`example` 表 + 内置配置（见下表），复用普通表生成链路产出 19 个文件 + 2 个枚举 + 2 个示例 POJO（`Tag` / `Profile`，供 jsonArray / jsonObject 绑定，保证开箱可编译）；示例代码已存在则整表跳过（**无强制覆盖选项**，创建过就跳过）；
4. **输出与报告**：示例表与其他表一起进入执行报告。

`generateExample: false`（默认）：不建表、不生成任何示例代码。

#### 内置示例配置（生成器内置，不要求用户手写）

```yaml
# 生成器内置（等效配置，非用户手写）
db_table_name: example
model_name: Example
model_comment: 示例
logicDelete:                    # 示例表强制演示逻辑删除（配置说明见 §8）
  enable: true
  column_name: del_flag
  normal_value: 0
  delete_value: 1
columns:
  user_type:
    type: enum
    enum:
      className: UserTypeEnum
      codeType: Integer
      values:
        - code: 0
          name: SYSTEM_USER
          desc: 系统用户
        - code: 1
          name: NORMAL_USER
          desc: 普通用户
  status:
    type: enum
    enum:
      className: UserStatusEnum
      codeType: String
      values:
        - code: ENABLED
          name: ENABLED
          desc: 启用
        - code: DISABLED
          name: DISABLED
          desc: 停用
  login_count:
    type: Integer
  tags:
    type: jsonArray
    javaObject: com.jakt.aiplatform.core.model.domain.Tag
  profile:
    type: jsonObject
    javaObject: com.jakt.aiplatform.core.model.domain.Profile
  # extra 列不配置 type，演示 json → String 默认映射
```

| 列 | 内置配置 | 演示点 |
| --- | --- | --- |
| `user_type` | `type: enum`（UserTypeEnum, Integer：0 系统用户 / 1 普通用户） | int 枚举 |
| `status` | `type: enum`（UserStatusEnum, String：ENABLED / DISABLED） | varchar 枚举 |
| `login_count` | `type: Integer` | bigint → int 强制转换（风险自负） |
| `tags` | `type: jsonArray` + `javaObject: Tag`（→ `List<Tag>`） | jsonArray + javaObject |
| `profile` | `type: jsonObject` + `javaObject: Profile`（→ `Profile`） | jsonObject + javaObject |
| `extra` | 不配置（json → String 默认映射） | json 原始字符串 |

示例表**强制配置逻辑删除**（§8 行为全链路演示：查询带 `del_flag = 0`、删除变 `UPDATE del_flag = 1`、更新/计数同样带条件）。示例即本设计文档 §4 / §5 的活演示：生成后可直接观察 DO 原始类型 vs Model 枚举类型、Convertor 转换代码、枚举 JSON 序列化对象形态、逻辑删除 SQL。

#### 校验与冲突

- `generateExample: true` 时必须提供 jdbc（建表与读元数据都需要），否则配置校验报错；
- 建表失败（无写权限等）→ fail-fast 报错退出；
- 示例表/示例代码已存在 → 跳过（`CREATE TABLE IF NOT EXISTS` + DO/枚举存在即跳过），进报告，无强制覆盖选项；
- 若用户同时在 `tables` 里配置了 `example` 表：以内置示例配置为准，重复配置报错提示。

## 6. 类型映射规则

### 6.1 默认映射（现状 + 补充）

未配置 `type` 的列按以下默认映射（json 列默认是原始字符串，不会自动解析）：

| 数据库类型 | Java 类型 |
| --- | --- |
| `json` | `String`（原始 JSON 文本） |
| 其余 | 与现状一致：bigint→Long、int/tinyint→Integer、varchar/char/text→String、datetime→LocalDateTime、date→LocalDate、decimal→BigDecimal、double→Double、bit→Boolean |

### 6.2 配置优先级

```
列配置（type: enum / jsonObject / jsonArray / Java类型） > 数据库默认映射
```

- 配置 `type: enum`：Model/DTO = 枚举类型，DO = code 原始类型；
- 配置 `type: jsonArray`：Model/DTO = `List<javaObject>`（缺省 `List<Object>`），DO = String；
- 配置 `type: jsonObject`：Model/DTO = `javaObject`（缺省 `Map<String, Object>`），DO = String；
- 配置 `type: json`：Model/DTO = String（与默认一致，显式声明仅用于可读性）；
- 配置 `type: <Java类型>`：Model/DTO = 指定类型，DO 不变（强制转换，风险自负）；
- 没配 `type`：与现状完全一致。

### 6.3 转换矩阵（Convertor 自动生成）

正向 = DO → Model，反向 = Model → DO。

**统一使用三方工具（Hutool），禁止手写判空与类型判断**：

- 数值/字符串互转：`cn.hutool.core.convert.Convert`（`Convert.toInt` / `toLong` / `toBigDecimal` / `toStr`，内部已处理 null）；
- 字符串是否为数字等判断：`cn.hutool.core.util.NumberUtil` / `StrUtil`（如 `NumberUtil.isNumber`、`StrUtil.isNumeric`），禁止手写正则或 `Integer.valueOf` 裸转换；
- 判空：`ObjectUtil` / `StrUtil` / `CollUtil`，禁止手写 `x == null ? ...` 风格（枚举 `getCode()` 反向除外，见下）。

Convertor 生成代码示例：

| DO（默认映射） | Model（目标） | 正向代码 | 反向代码 |
| --- | --- | --- | --- |
| 同类型 | 同类型 | 直接赋值 | 直接赋值 |
| Long | Integer | `Convert.toInt(do.getX())` | `Convert.toLong(model.getX())` |
| Integer | Long | `Convert.toLong(do.getX())` | `Convert.toInt(model.getX())` |
| String | Integer | `Convert.toInt(do.getX())` | `Convert.toStr(model.getX())` |
| String | Long | `Convert.toLong(do.getX())` | `Convert.toStr(model.getX())` |
| String | BigDecimal | `Convert.toBigDecimal(do.getX())` | `Convert.toStr(model.getX())` |
| Integer / String / Long | 枚举 | `XxxEnum.fromCode(do.getX())`（未匹配抛异常） | `ObjectUtil.isNull(model.getX()) ? null : model.getX().getCode()` |
| String(json) | `List<JavaObject>` | `JsonUtil.parseArray(x, JavaObject.class)` | `JsonUtil.toJson(x)` |
| String(json) | `List<Object>` | `JsonUtil.parseArray(x)` | `JsonUtil.toJson(x)` |
| String(json) | `JavaObject` | `JsonUtil.parseObject(x, JavaObject.class)` | `JsonUtil.toJson(x)` |
| String(json) | `Map<String, Object>` | `JsonUtil.parseMap(x)` | `JsonUtil.toJson(x)` |

`JsonUtil` 为 `common-util` 新增静态工具（内部 `ObjectMapper`；core-repository 引入 `jackson-databind`）。

> 泛型元素/目标类型（`Map<String, Object>`、`Set<String>`、`List<...>`）解析时，`JsonUtil` 提供 `parseArray(x, TypeReference)` / `parseObject(x, TypeReference)` 重载，Convertor 按 `javaObject` 的完整类型字符串生成 `new TypeReference<...>() {}`，不需要手写解析代码。

> 规则：Convertor 内出现的所有"判空 / 类型判断 / 类型转换"一律走 Hutool 或项目内工具（`AiPlatformInvoker` 等），生成代码里不允许出现裸 `Integer.valueOf`、手写 `== null ? ...`（枚举 code 反向是唯一允许的简单三元，且判空用 `ObjectUtil.isNull`）。

### 6.4 生成期校验（fail-fast）

| 场景 | 处理 |
| --- | --- |
| `columns` 键不是表内真实列 | 报错：`列 user_type 不存在于表 example` |
| `type` 不是支持值（enum/json/jsonArray/jsonObject/合法 Java 类型） | 报错 |
| `type: int` 等原生基础类型 | 不报错，自动归一化为包装类型（`Integer`），日志提示 |
| 普通列配置 `Map` / `Set` / `List` 集合类型 | 报错：集合类型仅用于 json 列 |
| `type: jsonObject` + `javaObject: String` | 报错：请改用 `type: json`（jsonArray + String 合法） |
| 枚举 code / name 重复 | 报错 |
| `codeType` 不在支持集合 | 报错 |
| 转换组合不在 §6.3 矩阵 | 报错：`不支持 bigint→LocalDateTime 转换（列 xxx）` |
| `type: jsonArray` / `jsonObject` 用在非 json 列 | 报错 |
| `javaObject` 不是合法全限定类名 | 报错 |

## 7. 枚举设计

### 7.1 新异常码

`ErrorCodeEnum` 新增（30000 段业务错误码）：

```java
/** 枚举值未匹配。 */
ENUM_NOT_MATCHED(30004, "枚举值未匹配"),
```

### 7.2 BaseEnum（core-model/enums 包，骨架静态文件）

```java
package com.jakt.aiplatform.core.model.enums;

import com.jakt.aiplatform.core.model.exception.AiPlatformException;

/**
 * 业务枚举基座。
 *
 * <p>T 为 code 类型（Integer / String / Long…），数据库只存 code；
 * name 为枚举常量名（如 SYSTEM_USER）；desc 为枚举描述（如 系统用户）。
 */
public interface BaseEnum<T> {

    /** 枚举 code（数据库存储值）。 */
    T getCode();

    /** 枚举常量名（如 SYSTEM_USER）。 */
    String getName();

    /** 枚举描述（如 系统用户）。 */
    String getDesc();

    /**
     * 按 Integer code 反查枚举；null 返回 null，未匹配抛 ENUM_NOT_MATCHED。
     */
    static <E extends BaseEnum<Integer>> E fromCode(Class<E> enumClass, Integer code) {
        if (code == null) {
            return null;
        }
        for (E e : enumClass.getEnumConstants()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        throw new AiPlatformException(ErrorCodeEnum.ENUM_NOT_MATCHED, "枚举值未匹配: " + code);
    }

    /**
     * 按 String code 反查枚举；null 返回 null，未匹配抛 ENUM_NOT_MATCHED。
     */
    static <E extends BaseEnum<String>> E fromCode(Class<E> enumClass, String code) {
        if (code == null) {
            return null;
        }
        for (E e : enumClass.getEnumConstants()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        throw new AiPlatformException(ErrorCodeEnum.ENUM_NOT_MATCHED, "枚举值未匹配: " + code);
    }

    /** 按 Long code 反查枚举；null 返回 null，未匹配抛 ENUM_NOT_MATCHED。 */
    static <E extends BaseEnum<Long>> E fromCode(Class<E> enumClass, Long code) {
        if (code == null) {
            return null;
        }
        for (E e : enumClass.getEnumConstants()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        throw new AiPlatformException(ErrorCodeEnum.ENUM_NOT_MATCHED, "枚举值未匹配: " + code);
    }

    /** 枚举是否等于指定 code。 */
    default boolean is(T code) {
        return getCode() != null && getCode().equals(code);
    }
}
```

> 已确认决策：**未知枚举 code 一律抛异常**（DB 脏数据与 Web 入参同规则），不再有"宽松返回 null"分支。

### 7.3 生成的枚举（templates/table/{EnumName}.java.ftl）

```java
package com.jakt.aiplatform.core.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**
 * 用户类型枚举。
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UserTypeEnum implements BaseEnum<Integer> {

    /** 系统用户。 */
    SYSTEM_USER(0, "系统用户"),

    /** 普通用户。 */
    NORMAL_USER(1, "普通用户");

    /** code（数据库存储值）。 */
    private final Integer code;

    /** 描述。 */
    private final String desc;

    UserTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name();
    }

    /**
     * 按 code 反查枚举；Jackson 反序列化入口。
     *
     * @param code code
     * @return 枚举
     */
    @JsonCreator
    public static UserTypeEnum fromCode(Integer code) {
        return BaseEnum.fromCode(UserTypeEnum.class, code);
    }
}
```

**序列化（已确认决策）**：`@JsonFormat(shape = OBJECT)` 让前端拿到的是 **JSON 对象**：

```json
{ "code": 0, "name": "SYSTEM_USER", "desc": "系统用户" }
```

而不是 `"SYSTEM_USER"` 字符串或 `@ENUMxxxx` 这类默认输出。这是"序列化 vs toString"问题的最优解：不改全局 Jackson 配置，收敛在枚举模板一处。

**反序列化**：`@JsonCreator fromCode(code)` 接收 code（标量）。实施时用冒烟测试锁定入参形态（code 标量 / `{"code":0}` 对象）与 OBJECT 形态的组合；若 Jackson 对"OBJECT 序列化 + delegating creator"存在冲突，回退方案是模板内生成自定义 `JsonSerializer` / `JsonDeserializer`（仍是收敛一处）。

**依赖**：`jackson-annotations` 以 `provided` 加入 `core-model`（spring-boot BOM 管版本），core-model 仍是"无运行期框架依赖"。

### 7.4 放置与冲突处理

- `BaseEnum.java` → `skeleton/core/model/src/main/java/{pkg}/core/model/enums/BaseEnum.java`（骨架静态文件）；
- 生成枚举 → 目标项目 `core/model/src/main/java/{pkg}/core/model/enums/{EnumName}.java`；
- 生成含枚举列的表时，若目标项目缺失 `BaseEnum.java`，自动从模板补写。
- **枚举文件冲突（已确认决策）**：
  - `force_create: false`：发现同名枚举已存在 → **跳过整张表**（不只是枚举文件），原因写入报告；
  - `force_create: true`：**覆盖**枚举文件，输出警告（⚠️ 强制覆盖）。

## 8. 逻辑删除

### 8.1 配置

两级配置，**表级优先级高于全局**：

```yaml
# 全局（顶层）
globalLogicDelete:
  enable: true          # true 时下面三项必填
  column_name: del_flag
  normal_value: 0
  delete_value: 1

# 表级（tables[].logicDelete，属性同全局，覆盖全局）
logicDelete:
  enable: true
  column_name: del_flag
  normal_value: 0
  delete_value: 1
```

- **前置校验（fail-fast）**：`enable: true` 时 `column_name` / `normal_value` / `delete_value` **三项必填**，缺失在生成前报错；
- **启用条件**：配置生效且 `column_name` 在表中真实存在才启用逻辑删除；未配置 / `enable: false` / 字段不存在 → 物理删除（现状）；
- **优先级**：表级 `logicDelete` > 全局 `globalLogicDelete`（针对具体表生效，其他表仍走全局配置）。

### 8.2 行为

假设配置 `column_name: del_flag, normal_value: 0, delete_value: 1`：

| 操作 | 无逻辑删除（现状） | 有逻辑删除 |
| --- | --- | --- |
| `selectById` | `WHERE id = #{id}` | `WHERE id = #{id} AND del_flag = 0` |
| `selectPage` / `selectList` / `countByQuery` | `queryConditions` | `queryConditions` 追加 `AND del_flag = 0` |
| `deleteById` | `DELETE FROM t WHERE id = #{id}` | `UPDATE t SET del_flag = 1 WHERE id = #{id} AND del_flag = 0` |
| `update` / `updateByCondition` | `WHERE id = #{id}` | `WHERE id = #{id} AND del_flag = 0` |
| `insert` | 原样 | 原样（del_flag 由数据库默认 0） |

`del_flag` 仍属于保留审计列（`create_by` / `update_by` / `del_flag` 不生成 DO 字段），由 SQL 层基础设施管理；`updateByCondition` 不更新 `del_flag`。

## 9. 内部表（不暴露 Controller）

`tables[].generateController: false`（默认 true）时，**该表只生成业务与数据层**：

```yaml
tables:
  - db_table_name: sys_job_log
    model_name: JobLog
    model_comment: 任务日志
    generateController: false   # 只加这一个开关，其他配置按需另加
```

| 产物 | `generateController: true`（默认） | `false` |
| --- | --- | --- |
| DO / Mapper / Mapper.xml | ✓ | ✓ |
| Model / QueryParam | ✓ | ✓ |
| Repository / RepositoryImpl / Convertor | ✓ | ✓ |
| Service / ServiceImpl | ✓ | ✓ |
| Manager / ManagerImpl | ✓ | ✓ |
| Controller / web DTO（Create/Update/Query/Response）/ Assembler / ParamChecker | ✓ | ✗ |

理由：web DTO / Assembler / ParamChecker 只服务于 Controller，无 Controller 时是死代码，不生成。`QueryParam` 属于 core-model（Repository/Service 查询用），保留。

## 10. 控制台输出与执行报告

### 10.1 表级消息（替代逐文件绝对路径打印）

```text
[gen] 开始生成 2 张表
[gen] example(示例) 表创建成功
[gen] example(示例) 表代码生成成功（21 个文件，另生成 2 个示例 POJO）
[gen] sys_job_log 表已存在，跳过（DO 已存在；如需覆盖请配置 force_create: true）
[gen] ⚠️  member 表存在，强制覆盖（会覆盖手动修改的代码！）
[gen] ⚠️  dept 表已存在枚举 DeptTypeEnum，跳过整表（如需覆盖请配置 force_create: true）
```

不再打印 `生成 /com/jakt/aiplatform/...` 这类路径噪声；逐文件细节只在 `force_create` 覆盖时提示。

### 10.2 执行报告框

**出现跳过或警告时**，结束时在 Terminal 输出报告框：

```text
┌─────────────────────────── 执行报告 ───────────────────────────┐
│ 成功: 1    跳过: 2    警告: 1                                  │
├────────────────────────────────────────────────────────────────┤
│ [跳过] sys_job_log: DO 已存在，未配置 force_create              │
│ [跳过] dept: 枚举 DeptTypeEnum 已存在，未配置 force_create     │
│ [警告] member: 已存在，force_create 强制覆盖                    │
└────────────────────────────────────────────────────────────────┘
```

**全部成功时**不画框，只输出一行 `[gen] 全部 N 张表生成成功`。

### 10.3 统计口径

- 成功：该表全部文件生成完成；
- 跳过：DO 已存在 / 枚举已存在（未 force_create）；
- 警告：force_create 强制覆盖（含覆盖枚举）。

示例表（`generateExample`）与其他表同口径统计，不单独区分。

## 11. 分层落点（模板改动）

| 产物 | 枚举列 | jsonArray 列 | 强制类型列 | 逻辑删除影响 |
| --- | --- | --- | --- | --- |
| `XxxDO`（common-dal） | 原始 code 类型，不变 | String，不变 | 原始类型，不变 | 不变（del_flag 不入 DO） |
| `XxxMapper.xml` | 无改动 | 无改动 | 无改动 | 按 §8.2 追加 del_flag 条件 / delete 变 update |
| `Xxx` Model（core-model） | 枚举类型 + import | `List<JavaObject>` | `type` 覆盖 | 无 |
| `XxxQueryParam` / `XxxQueryRequest` | **保持原始 code 类型** | String | 原始类型 | 无 |
| `XxxConvertor`（repository） | `XxxEnum.fromCode` / `ObjectUtil.isNull(...) ? null : getCode()` | `JsonUtil.parseArray(x, T.class)` / `toJson` | 按 §6.3（Hutool） | 无 |
| `XxxCreateRequest` / `XxxUpdateRequest` | 枚举（`@JsonCreator` 收 code） | `List<JavaObject>` | `type` 覆盖 | 无 |
| `XxxResponse` | 枚举（`@JsonFormat OBJECT` 出 json 对象） | `List<JavaObject>` | `type` 覆盖 | 无 |
| `XxxAssembler` | 枚举直接透传 | 直接透传 | 直接透传 | 无 |
| `XxxParamChecker` | 无新增（fromCode 已抛错兜底） | — | — | 无 |

> **Query 保持原始 code 的原因**：MyBatis 默认 `EnumTypeHandler` 按枚举 `name()` 绑定参数，与 int/varchar 列不匹配；查询按 code 最稳，也最符合"数据库存什么就查什么"。

新增模板：

- `templates/table/{EnumName}.java.ftl`：生成枚举（§7.3）；
- `templates/base/BaseEnum.java.ftl`：BaseEnum 补写模板。

## 12. 生成器实现改动

1. **GeneratorConfig**：
   - 顶层新增 `generateExample`、`globalLogicDelete`（enable / column_name / normal_value / delete_value）；
   - `TableConfig` 新增 `generateController`、`logicDelete`（覆盖全局）、`Map<String, ColumnConfig> columns`；
   - 前置校验：`enable: true` 时逻辑删除三件套必填；`generateExample: true` 时 jdbc 必填；
   - `ColumnConfig`：`type`（enum / json / jsonArray / jsonObject / Java 类型）、`javaObject`、`EnumConfig`（className/codeType/values）。
2. **ColumnMeta**：新增 `modelType`、`enumInfo`、`conversion`（NONE / ENUM / JSON / JSON_ARRAY / JSON_OBJECT / COERCE）、`jsonElementType`；`javaType` 保留为"DO 类型（数据库默认映射）"。
3. **DbMetaReader**：读表后合并列配置 → 计算 `modelType` + `conversion` + import；按 §6.4 校验；检测逻辑删除字段是否存在于表结构。
4. **CrudGenerator**：
   - 表级判定：DO 已存在或枚举已存在且非 force_create → 跳过整表；
   - `generateController: false` 时过滤 web 层模板；
   - 逐表输出友好消息，汇总 成功/跳过/警告，结束时按 §10 画报告框。
5. **模板**：按 §11 改造；Convertor 按 `conversion` 分支生成转换代码；Mapper.xml 按逻辑删除开关生成 SQL；枚举模板含 `@JsonFormat(OBJECT)` + `@JsonCreator`。
6. **buildSqlFragments**：不变（基于 DO 列名/属性名；逻辑删除条件在 Mapper.xml 模板拼接）。
7. **示例生成**：GeneratorConfig 增加 `generateExample`；Main 在骨架初始化后、普通表生成前执行"示例流程"——执行 `skeleton/sql/example.sql` 建表（IF NOT EXISTS）→ 注入内置 example 表配置（§5.5，含 `logicDelete`、javaObject 绑定）→ 生成 `Tag` / `Profile` 两个示例 POJO → 并入 CrudGenerator 统一生成与报告；示例表/代码已存在则跳过。
8. **generate.yaml.example / README / AGENTS.md**：同步示例与约定（含"内部表不生成 Controller"、"逻辑删除"、"强制转换风险提示"、"generateExample 用法"）。

## 13. 风险与边界

- **强制类型转换有编译/溢出风险**（`bigint→int` 溢出、`varchar→int` NumberFormatException）：自由度由用户显式选择；生成器保证矩阵内组合可编译，语义正确性由配置人负责。
- **JSON 转换运行期异常**：非法 json 文本在 Convertor 反序列化时抛错，全局异常处理器兜底。
- **枚举未匹配抛异常**：DB 脏数据会导致查询报"枚举值未匹配"，这是已确认的决策；数据治理是业务职责。
- **core-model 依赖面**：新增 `jackson-annotations`（provided），编译期轻依赖。
- **逻辑删除是 SQL 层约定**：del_flag 值约定 0/1（可配置）；`del_flag` 列不生成 DO 字段，手工 SQL 需自行维护约定。
- **向后兼容**：不配置新选项时输出与现状逐字节一致。
- **示例建表需要数据库写权限**：`generateExample: true` 会执行 DDL；无权限时生成失败并提示。
- **示例表与业务表同名冲突**：`tables` 中出现 `example` 表时以内置示例配置为准并报错提示，避免两套定义漂移。

## 14. 实施步骤

1. **骨架瘦身**：删除 skeleton 全部 User 业务代码；`skeleton/sql/example.sql` 放入 §4 示例 DDL（含 `del_flag`）；改写 skeleton README / AGENTS.md（分层讲解不再依赖 User 模块）。
2. `ErrorCodeEnum` 增加 `ENUM_NOT_MATCHED`；`BaseEnum.java` 入骨架；`core-model` pom 加 `jackson-annotations`（provided）。
3. `GeneratorConfig` 解析全局/表级/列级配置 + `generateExample` + 前置校验（逻辑删除三件套、jdbc）。
4. `DbMetaReader` 合并列配置、逻辑删除检测，扩展 `ColumnMeta`。
5. 新模板：`{EnumName}.java.ftl`、`BaseEnum.java.ftl`；改造 Convertor / Model / DTO / Assembler / Mapper.xml / Controller 模板。
6. `CrudGenerator` 表级跳过判定、`generateController` 过滤、友好消息与报告框；Main 增加示例流程（建表 + 内置配置注入 + 示例 POJO 生成）。
7. 更新本地 `example` 表（§4 新结构，含 `del_flag`）与 `generate.yaml.example`（§5.4）。
8. 验证：`generateExample: true/false` 两种模式各跑一遍（输出 diff + `mvn package` 编译 + 启动冒烟：枚举读写 JSON 对象形态、逻辑删除、内部表无 Controller、报告框）。

## 15. 关于 MySQL 原生 enum 的说明（问答）

业务代码里**原生 `enum` 类型用得不多**，常见做法是 `int` / `tinyint` / `varchar` + 代码枚举或字典表，原因：

- 加一个枚举值要 `ALTER TABLE`，改表结构成本高；int/varchar 加值是纯代码改动；
- 原生 enum 排序、`GROUP BY`、索引按"定义顺序"而非字典序，容易踩坑；
- 跨数据库迁移（如 MySQL → PostgreSQL / TiDB）时 enum 类型不通用；
- ORM 工具链对原生 enum 的映射支持参差不齐。

所以本期设计：原生 enum 列默认按 `String` 映射，需要枚举时走列配置（与 int/varchar 枚举完全一致），**不自动解析 `COLUMN_TYPE`**。
