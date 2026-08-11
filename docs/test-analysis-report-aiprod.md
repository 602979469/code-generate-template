# AiProd 白盒测试分析报告（code-generate-template 功能验证）

> 测试对象：code-generate-template 代码生成器（Java 17 + Freemarker + SnakeYAML + MySQL）
> 验证方式：**白盒**——逐类走读生成器源码，将每个功能点映射到具体代码路径，再通过实际生成 AiProd 项目 + 产物核对 + 编译 + 运行期 HTTP 冒烟逐项印证。
> 日期：2026-08-11
> 生成器基线：git `8d9260d` + 工作区未提交改动（主键不假设 `id`、按 PRIMARY KEY 元数据识别等 v2.1 能力）
> 测试输出项目：`/Users/jakt/IdeaProjects/AiProd`
> 测试库：`ai_prod`（生成期读表结构）/ `aiprod`（运行期，见 §7 F1）

---

## 1. 代码路径总览（白盒地图）

生成器执行链与源码一一对应：

```
./gen.sh <config.yaml>
  └─ java -Dcgt.templateRepo=<repo> -jar generator/target/generator.jar <config.yaml>
      ├─ Main.main()                                  # 编排入口
      │   ├─ GeneratorConfig.load()                   # YAML 解析
      │   ├─ GeneratorConfig.validateNaming()         # 前置校验（fail-fast）
      │   ├─ SkeletonGenerator.run()                  # 1) 项目骨架初始化（token 替换）
      │   ├─ ExampleGenerator.createTable()           # 2a) generateExample: 建 example 表
      │   ├─ ExampleGenerator.generatePojos()         # 2b) 生成 Tag/Profile POJO
      │   ├─ ExampleGenerator.exampleTableConfig()    # 2c) 注入内置示例表配置
      │   └─ CrudGenerator.run()                      # 3) 按 tables 逐表生成 CRUD
      │       ├─ DbMetaReader.read()                  # 读 information_schema + 合并列配置
      │       └─ Freemarker 渲染 templates/table/*.ftl（19 个 + 枚举模板）
      └─ System.exit(1) / 执行报告框
```

| 模块 | 职责 | 关键代码点 |
| --- | --- | --- |
| `Main.java` | 单入口编排；重复配置 example 检测；异常统一兜底 | `main()` 三阶段调用 |
| `GeneratorConfig.java` | YAML 解析；命名/表/列/逻辑删除四级校验 | `load()` / `validateNaming()` / `validateColumnConfig()` |
| `SkeletonGenerator.java` | 复制 skeleton + 5 组 token 替换；跳过 target/.git/.idea/out | `TOKENS` 顺序替换表、`isSkipped()` |
| `ExampleGenerator.java` | 建表、示例 POJO、内置 example 表配置注入 | `exampleTableConfig()` 全功能列配置 |
| `DbMetaReader.java` | 表存在性校验、主键识别、类型映射、列级配置、SQL 片段 | `read()` / `applyColumnConfigs()` / `buildSqlFragments()` / `resolveLogicDelete()` |
| `CrudGenerator.java` | 逐表渲染、防覆盖/强制覆盖、报告框 | `TEMPLATES` 映射表、`render()` / `renderEnums()` / `generateTableSql()` |
| `templates/table/*.ftl` | 19 类产物模板（DO/Mapper/XML/Model/QueryParam/Repository/Convertor/Service/Manager/DTO/Assembler/ParamChecker/Controller） | `${pkColumnName}` 等占位符贯穿 |

---

## 2. 测试配置

| 配置文件 | 用途 | 覆盖功能点 |
| --- | --- | --- |
| `test-configs/generate.aiprod.yaml` | 主配置：AiProd 项目 + generateExample + 3 张业务表 | 骨架、示例全功能、非 `id` 主键、char 逻辑删除、内部表 |
| `test-configs/generate.aiprod-key.yaml` | sys_test_key（varchar 非自增主键） | `pkAuto=false` 分支：insert 显式主键、DTO @NotBlank、无 useGeneratedKeys |
| `test-configs/scratch/neg-aiprod-*.yaml` ×3 | 负向用例 | 缺命名、复合主键、表不存在 |
| （临时）sys_cost_enum Long 枚举 | D1 回归 | `codeType: Long` 枚举生成与编译 |

主配置中的 3 张业务表分别对应当前代码的关键分支：

- **sys_dept**（PK=`dept_id` bigint 自增、`del_flag` char(1)、含 `create_by/update_by`）→ 非 `id` 主键识别、保留列剔除、**字符串列逻辑删除值加引号**、表级 logicDelete 覆盖全局；
- **sys_user**（PK=`user_id`、`login_name` NOT NULL 无默认值）→ 必填校验注解、多字段大表回归；
- **sys_config**（PK=`config_id` int、无 del_flag、`generateController: false`）→ 内部表裁剪（12 文件）、无逻辑删除列时退化物理删除。

---

## 3. 白盒验证结果

### 3.1 执行编排（Main）✅

- 无参数 → 打印用法；缺文件 → `配置文件不存在`；配置加载/校验/生成任一失败 → `[gen] 执行失败: <原因>` + exit 1。
- 实测：3 个负向配置全部 `exit=1` 且错误信息定位准确（§5）。

### 3.2 配置加载与前置校验（GeneratorConfig）✅

| 代码点 | 预期 | 实测 |
| --- | --- | --- |
| `validateNaming()` 四件套必填 | 缺 projectPrefix 报 `配置缺少必填项` | ✅ `配置缺少必填项: projectPrefix` |
| `projectArtifactPrefix` 正则 `[a-z][a-z0-9]*` | 连字符/大写拦截 | ✅ 文案正确 |
| tables 项 `db_table_name/model_name/model_comment` 必填 | 缺项 fail-fast | ✅ |
| jdbc 存在性（tables 或 generateExample 时） | 缺 url/username 报错 | ✅ |
| `logicDelete.enable=true` 时三值必填 | 缺值报错 | ✅ |
| 列级配置校验（type 白名单、枚举块完整性、code/name 去重、jsonObject+String 非法、集合类型仅限 json 列） | 全部 fail-fast | ✅（`validateColumnConfig()` 全分支） |
| 原生类型归一化（`int→Integer` 等） | `normalizeJavaType()` 归一后走包装类型 | ✅ |

### 3.3 项目骨架初始化（SkeletonGenerator）✅

- **token 替换顺序**：`AiplatformApplication→AiProdApplication`、`com.jakt.aiplatform→com.jakt.aiprod`、`AiPlatform→AiProd`、`aiplatform→aiprod`、`com.jakt→com.jakt`。
- 实测：`AiProdApplication` 包名/扫描路径正确；全项目 grep 无 `com.jakt.aiplatform`/`AiPlatform`/`aiplatform` 残留；Maven 占位符 `${java.version}` 等原样保留。
- **防覆盖**：重复初始化输出 `生成 0 个文件，跳过 50 个`，不覆盖已存在文件。
- **跳过规则**：`isSkipped()` 过滤 target/.git/.idea/out；skeleton 无 User 业务代码（纯基础设施）。

### 3.4 示例代码开关（ExampleGenerator）✅

- `generateExample: true`：自动建 example 表（幂等）→ 生成 Tag/Profile POJO → 注入内置表配置（2 个枚举、login_count 强制 Integer、tags jsonArray、profile jsonObject、强制逻辑删除）→ 走普通表链路输出 **21 个文件**（19 + UserTypeEnum + UserStatusEnum）。
- 重复运行：example 整表跳过（DO 已存在），进报告框。
- 用户 tables 重复配置 example → `Main` 前置 `IllegalArgumentException` 拦截（旧 D3 已修复）。

### 3.5 表结构读取与元数据构建（DbMetaReader）✅（重点白盒点）

**主键识别（本次工作区核心改动）**

- 代码：`read()` 遍历 `information_schema.columns`，按 `column_key=PRI` 识别主键，不再假设 `id`；复合主键抛 `复合主键，暂不支持`；缺主键抛 `缺少主键`；缺 `create_time/update_time` 抛强约束错误。
- 实测：
  - `sys_dept` → `DeptDO` 生成 `private Long deptId`（无 `id` 字段），Mapper 全部 `WHERE dept_id = #{id}`、`ORDER BY dept_id DESC`、`keyProperty="deptId"`；
  - `sys_user` → `userId`；`sys_config` → `configId`（int）；
  - `sys_role_menu`（复合主键）→ `表 sys_role_menu 为复合主键，暂不支持` fail-fast，**且未落任何半成品文件**（旧 D2 修复回归通过）。

**类型映射与保留列**

- `mapJavaType()` 默认矩阵 + `BASE_COLUMNS/RESERVED` 剔除：`DeptDO`/`UserDO` 均无 `createBy/updateBy/delFlag`；`selectColumns/insertColumns/updateSet` 同步剔除（DeptMapper 的 selectColumns 结尾为 `..., status, create_time, update_time`）。
- `sys_notice` 的 `longblob` 等未显式映射类型 → 默认 String（代码 default 分支）。

**列级转换（applyColumnConfigs）**

- Example 实测：DO 保持原始类型（`user_type Integer / status String / tags String / profile String`），Model 层 `UserTypeEnum/UserStatusEnum/List<Tag>/Profile/loginCount Integer`；
- `ExampleConvertor`：`UserTypeEnum.fromCode(...)`、`ObjectUtil.isNull(...) ? null : getCode()`、`JsonUtil.parseObject/parseArray`、`Convert.toInt/toLong`，无裸 `Integer.valueOf`；
- 查询层保持原始 code 类型：`ExampleQueryParam` 中 `userType/status` 仍为 Integer/String。

**逻辑删除（resolveLogicDelete）**

- 两级配置：表级 > 全局；`enable=true` 且列真实存在才启用，否则物理删除。
- **字符串列值加引号**：sys_dept `del_flag` 为 char(1)，生成 `AND del_flag = '0'`、`SET del_flag = '2'`（表级覆盖全局的 '1'，引号正确）；
- sys_user（走全局）→ `del_flag = '1'`；sys_config 无 del_flag 列 → 物理 `<delete>`；
- insert 不写 del_flag；select/update/count 均带 `AND del_flag = '0'`。

**SQL 片段（buildSqlFragments）**

- 自增主键：insert 不含主键列 + `useGeneratedKeys`（deptId/userId/configId）；**非自增主键**（sys_test_key）：insert 含 `code` 列、无 useGeneratedKeys，实测 `INSERT INTO sys_test_key (code, value_text) VALUES (#{code}, #{valueText})`。

### 3.6 表级生成（CrudGenerator）✅

| 验证点 | 实测 |
| --- | --- |
| 标准表 19 文件 | sys_dept / sys_user / sys_test_key 各 19 ✅ |
| `generateController: false` 内部表 | sys_config 仅 12 文件（无 Controller/ParamChecker/3 DTO/Assembler）✅ |
| 枚举文件 | example 21（19+2 枚举）、sys_cost_enum 20（19+1 枚举）✅ |
| 防覆盖 | 主配置重复运行 → `成功 0 / 跳过 4 / 警告 0` 报告框 ✅ |
| 每表 SQL 落盘 | `sql/{表名}.sql` 为真实 SHOW CREATE TABLE ✅ |
| 控制台报告 | 全部成功不画框；有跳过画框 ✅ |

### 3.7 模板产物核对

- `DeptDO`：`/** 主键。 */ private Long deptId;` + 业务字段 + 继承 BaseDO（createTime/updateTime）；
- `TestKeyCreateRequest`：`@NotBlank(message = "code不能为空")` + `@Size(max=200)`；`DeptCreateRequest`：无 @NotBlank（dept_name 可空带默认值），varchar 全有 @Size——与 `required = NOT NULL && 无默认值` 的代码规则一致；
- `UserCreateRequest`：`login_name` → `@NotBlank(message = "登录账号不能为空")`；
- `TestKeyController`：`@PathVariable String id`；`DeptController`：`@PathVariable Long id`，`deptManager.getDept(id)` + `AiProdInvoker.throwErrWhenNull`；
- `TestKeyAssembler`/`Convertor`：`setCode(...)` 显式赋值。

---

## 4. 编译验证 ✅

```
cd /Users/jakt/IdeaProjects/AiProd && mvn -q -DskipTests package
```

- 全模块（bootstrap/web/biz/core/common × 6 张表产物）编译通过，exit 0；
- 含 Long 枚举（CostTimeEnum：`FAST(0L, "快速")`、`implements BaseEnum<Long>`）——**旧缺陷 D1 回归通过**（`DbMetaReader` Long 加 `L` 后缀 + BaseEnum 新增 Long 重载）。

## 5. 负向用例结果

| 用例 | 配置 | 结果 |
| --- | --- | --- |
| 缺 projectPrefix | `neg-aiprod-missing-naming.yaml` | exit 1：`配置缺少必填项: projectPrefix` ✅ |
| 复合主键 | `neg-aiprod-composite-pk.yaml`（sys_role_menu） | exit 1：`表 sys_role_menu 为复合主键，暂不支持`，无半成品文件 ✅ |
| 表不存在 | `neg-aiprod-missing-table.yaml` | exit 1：`表不存在: not_exist_table`，无半成品文件 ✅ |

## 6. 运行期冒烟（Spring Boot 4.0.6，1.8s 启动，端口 8081）

| # | 用例 | 结果 |
| --- | --- | --- |
| 1 | Dept 分页（deptId 主键 + del_flag 过滤） | ✅ total=2，按 dept_id DESC |
| 2 | Dept 按 deptId 查询 | ✅ 返回 deptId=100 |
| 3 | Dept 创建（自增主键回填） | ✅ deptId=110 |
| 4 | Dept 更新 | ✅ |
| 5 | Dept 逻辑删除（表级 delete_value='2'） | ✅ DB `del_flag=2` |
| 6 | 删除后查询 | ✅ 30001「部门不存在」 |
| 7 | User 校验（缺 login_name） | ✅ 20000「登录账号不能为空」 |
| 8 | User 创建/分页（userId 主键） | ✅ userId=10 |
| 9 | User 逻辑删除（全局 delete_value='1'） | ✅ DB `del_flag=1`，再查 30001 |
| 10 | Example 查询 id=1（枚举对象/JSON 还原/强制转换） | ✅ `userType:{code:0,name:SYSTEM_USER,desc:系统用户}`、profile/tags 还原、loginCount=5(Integer) |
| 11 | Example 全字段创建（枚举对象入参） | ✅ 落库原始 code + JSON 字符串 |
| 12 | 未知枚举 code=99 | ✅ 30004「枚举值未匹配: 99」 |
| 13 | Example 缺默认值列（level/loginCount/balance） | ⚠️ 20000「数据不合法：必填字段缺失或违反数据约束」（见 §7 F2） |
| 14 | TestKey 非自增主键创建/查询 | ✅ code=KEY001 全链路 |
| 15 | 内部表无 Controller | ✅ 真实 HTTP 404（旧 D7 已修复） |

---

## 7. 发现与建议

### F1（P2，新发现）运行期数据源库名与生成期 jdbc.url 解耦，由 token 替换决定

- **现象**：`generate.yaml` 的 `jdbc.url` 指向 `ai_prod`（生成器读表结构用），但生成的 `AiProd/application.yml` 中数据源为 `.../aiprod`——库名来自 skeleton 中 `aiplatform→aiprod` 的 token 替换（`SkeletonGenerator.TOKENS` 第 4 组），与配置无关。
- **证据**：`skeleton/.../application.yml` 硬编码 `/aiplatform`；AiProd 产物为 `/aiprod`；本机无 `aiprod` 库（本次验证已手动补建同名库完成冒烟）。
- **影响**：只要 `projectArtifactPrefix` ≠ 目标库名（如 RuoYi 风格 `ai_prod` vs artifact `aiprod`），生成项目开箱连不上库；aiplatform 项目未暴露是因为 artifact 恰好等于库名。
- **建议**：骨架数据源改为 `${DB_NAME:aiplatform}` 环境变量占位，或在文档明示「运行期库名 = projectArtifactPrefix，需建同名库或改 application.yml」。

### F2（P3，旧 D5 缓解后遗留）默认值列缺失时报错不指明字段

- NOT NULL 且有默认值的列（example.level/login_count/balance）不生成 @NotNull（符合 `required` 规则），但 INSERT 全列直传 NULL → `DataIntegrityViolationException` → 已由异常处理器映射为 20000 通用文案「数据不合法：必填字段缺失或违反数据约束」。
- 相比旧报告（曾为 10000 SYSTEM_ERROR）已是修复态，但文案不指出具体缺失字段；若接受「全量语义」，可对默认值 NOT NULL 列补 @NotNull，或将异常文案细化到列。

### F3（P3，风格小瑕疵）枚举 javadoc 拼接偶发不通顺

- 枚举模板 javadoc 为 `${entityName}${enumDesc}枚举`：`model_comment=耗时枚举` + 列注释「耗时等级」→ 「耗时枚举耗时等级枚举」。功能无影响，纯文案。

### F4 旧缺陷回归状态

| 旧缺陷 | 状态 | 证据 |
| --- | --- | --- |
| D1 Long 枚举不可编译 | ✅ 已修复 | `FAST(0L)` + `BaseEnum<Long>`，编译通过 |
| D2 不存在表先落半成品 | ✅ 已修复 | 负向用例无 `NotExist*` 文件 |
| D3 tables 重复 example 未报错 | ✅ 已修复 | `Main` 前置检测代码存在 |
| D4 错误文案偏差 | ✅ 已修复 | 列级校验统一为「type 不是支持的值」口径 |
| D5 默认值列缺失→10000 | ✅ 缓解（→20000，见 F2） | 冒烟 #13 |
| D6 查询 null NPE | ✅ 已修复 | 删除后查询稳定返回 30001（Controller `throwErrWhenNull` 路径） |
| D7 未映射路由返回 200+10000 | ✅ 已修复 | 冒烟 #15 真 HTTP 404 |
| D8 Controller 残留 import | ✅ 已修复 | 产物无 `ParameterMetaData` import |

---

## 8. 结论

对当前工作区代码（含未提交的「主键按 PRIMARY KEY 元数据识别」改动）做白盒走读 + AiProd 实生成验证，**12 项功能模块全部通过**：骨架 token 替换、示例代码开关、非 `id` 主键（dept_id/user_id/config_id/varchar 非自增）、保留列剔除、字符串列逻辑删除引号、列级枚举/json/强制转换、内部表裁剪、防覆盖与报告框、负向 fail-fast、全工程编译、运行期 CRUD/校验/枚举/逻辑删除全链路。旧报告 8 项缺陷中 7 项确认修复，1 项缓解（F2）；新发现 1 项设计耦合（F1）建议文档化或改环境变量占位。

> 附：AiProd 项目为本次验证产物，含 6 张表生成代码（example/sys_dept/sys_user/sys_config/sys_test_key/sys_cost_enum）；测试配置保留于 `test-configs/`；`ai_prod` 库新增 2 张测试表（sys_test_key、sys_cost_enum），`aiprod` 库为运行期冒烟补建（含同结构表与种子数据）。
