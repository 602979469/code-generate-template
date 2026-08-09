# code-generate-template

项目样板 + 表级 CRUD 代码生成器。所有模板、生成器、工程约定都维护在本仓库，业务项目只接收生成结果。

## 目录结构

```
code-generate-template/
├── generator.properties   # 生成配置（项目前缀、包名、jdbc、表名前缀）
├── gen.sh                 # 命令行入口
├── generator/             # 生成器本体（Java + Freemarker + MySQL，打成 fat jar）
├── skeleton/              # 项目初始化样板（新项目 = 复制 + 改名）
└── templates/table/       # 表级 CRUD 样板（${className}DO / Mapper / XML / ... / Controller）
```

## 用法

```bash
# 初始化新项目：AiProd（包名 com.jakt.aiprod）
./gen.sh init -p AiProd -g com.jakt -o ../AiProd

# 新表生成 CRUD（mapper -> controller 共 16 个文件）
./gen.sh table -t sys_dept,member -o /path/to/目标项目

# 已存在文件默认跳过，-f 强制覆盖
./gen.sh table -t sys_dept -f -o /path/to/目标项目

# 列出表级模板
./gen.sh list
```

## 占位符与改名规则

### 表级模板（Freemarker，`${}` 占位）

| 变量 | 含义 | 示例 |
| --- | --- | --- |
| `${projectPrefix}` | 项目前缀类名（Application 等） | AiProd |
| `${toolPrefix}` | 工具类/异常/常量前缀 | AiPlatform |
| `${basePackage}` | 基础包名（= groupId.artifactId） | com.jakt.aiprod |
| `${className}` | 表对应的类名（去掉 tablePrefix） | sys_dept -> Dept |
| `${tableName}` | 表名 | sys_dept |
| `${columns}` / `${queryColumns}` / `${requiredColumns}` | 字段元信息（由表结构自动解析） | - |
| `${selectColumns}` / `${insertColumns}` / `${updateSet}` | SQL 片段（自动拼装） | - |

### 项目初始化（skeleton，token 替换）

skeleton 是"能编译的真实代码"，生成时按顺序做 token 替换，所以 pom 里的 `${java.version}` 等 Maven 占位符原样保留：

| token | 替换为 |
| --- | --- |
| `AiplatformApplication` | `${projectPrefix}Application` |
| `com.jakt.aiplatform` | 基础包名 |
| `AiPlatform` | `${toolPrefix}`（工具类/异常/常量） |
| `aiplatform` | `${projectArtifactPrefix}` |
| `com.jakt` | `${groupId}` |

因此 AiPlatformException -> AiProdException、AiPlatformInvoker -> AiProdInvoker 自动完成，异常/工具类互相引用一致。

## 表级生成说明

- 强约束：表必须包含 `id` / `create_time` / `update_time`（对应 BaseDO）；`create_by` / `update_by` / `del_flag` 为保留审计列，当前不生成，后续由 BizDO 扩展。
- 字段类型映射：bigint->Long、int/tinyint->Integer、varchar/char/text->String、datetime->LocalDateTime、decimal->BigDecimal。
- 查询条件：varchar 列默认 LIKE，其他列 EQ，`status` 列 EQ；`remark/password/salt/avatar` 不进查询条件。
- 必填校验：NOT NULL 且无默认值的列进入 DomainService 的 `throwErrWhenBlank/throwErrWhenNull` 校验。
- 输出 16 个文件：DO、Mapper、Mapper.xml、Model、QueryParam、Repository、RepositoryImpl、Convertor、DomainService、BizService、Controller、4 个 DTO、Assembler。

## 维护约定

- 改生成风格：只改 `templates/table/*.ftl` 或 `skeleton/`，改完跑一次生成 + git diff 评审。
- 新增好用的工具类/依赖：加进 `skeleton/`，新项目自动继承。
- 团队统一：模板仓库打 tag，各项目按固定 tag 生成，避免版本漂移。
