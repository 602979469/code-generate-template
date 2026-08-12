# code-generate-template

**项目样板 + 表级 CRUD 代码生成器**：通过一个 YAML 配置文件，初始化可编译的多模块 Spring Boot 工程，并按数据库表生成完整分层的 CRUD 代码。

模板、生成器与工程约定全部维护在本仓库；业务项目只接收生成结果，不依赖本仓库运行。

## 核心能力

- **一键初始化工程**：复制 `skeleton/` 样板并按命名规则替换（启动类名、基础包名、工具类前缀、artifactId），产出 bootstrap / web / biz / core / common 多模块可编译工程，Maven 占位符原样保留；
- **表级 CRUD 全分层生成**：每张表 19 个文件（DO → Mapper → Model → Repository → Service → Manager → Controller），内部表可裁剪为 12 个数据/业务层文件；
- **类型映射**：数据库默认映射 + 列级配置（枚举 / json / jsonArray / jsonObject / 强制类型转换），DO 保持数据库原始类型，转换全部收敛在仓储 Convertor；
- **逻辑删除**：全局 + 表级两级配置，查询/更新自动过滤、删除变 UPDATE；未配置或列不存在自动退化为物理删除；
- **防覆盖与执行报告**：已存在文件默认跳过（重复运行幂等），`force_create` 强制覆盖并警告，结束时输出成功/跳过/警告报告；
- **工程约定内置**：统一日志（LoggerUtil）、统一返回体与 Template 封装、参数校验、全局异常分类（404/405/400/业务错误码）；
- **实测可用**：生成项目编译通过、可启动；CRUD / 枚举 / JSON / 逻辑删除 / 错误码全链路冒烟验证通过。

## 目录结构

```
code-generate-template/
├── README.md                       # 项目介绍（本文件）
├── 代码生成器配置文件使用说明.md     # 配置项与生成行为详解（映射逻辑为重点）
├── generate.yaml.example           # 生成配置模板（含 3 张示例表，覆盖全部配置项）
├── gen.sh                          # 命令行入口（首次运行自动构建生成器）
├── generator/                      # 生成器本体（Java + Freemarker + MySQL，打成 fat jar）
├── skeleton/                       # 项目初始化样板（新项目 = 复制 + 改名）
│   └── sql/example.sql             # 示例表 DDL（内部表 / 逻辑删除 / 全功能表）
└── templates/table/                # 表级 CRUD 模板（DO / Mapper / Model / ... / Controller）
```

## 快速开始

前置要求：JDK 17+、Maven 3.9+、可连接的 MySQL。

```bash
# 1) 生成配置模板（当前目录 generate.yaml + example.sql）
./gen.sh

# 2) 建示例表（可选：只跑自己的业务表则跳过）
mysql -uroot -p < ./example.sql

# 3) 编辑 generate.yaml：项目命名 / jdbc / outputDir / tables
# 4) 按配置生成：初始化骨架（已存在跳过）+ 按 tables 生成 CRUD
./gen.sh ./generate.yaml
```

所有配置项与生成行为的详细说明见 [代码生成器配置文件使用说明.md](代码生成器配置文件使用说明.md)，配置模板见 [generate.yaml.example](generate.yaml.example)。

## 生成产物一览

| 表类型 | 文件数 | 范围 |
| --- | --- | --- |
| 标准表 | 19 | DO / Mapper / Mapper.xml / Model / QueryParam / Repository / RepositoryImpl / Convertor / Service / ServiceImpl / Manager / ManagerImpl / Controller / ParamChecker / CreateRequest / UpdateRequest / QueryRequest / Response / Assembler |
| 内部表（`generateController: false`） | 12 | 去掉 web 层 7 个文件 |
| SQL | 每表 1 个 | `sql/{表名}.sql` = `SHOW CREATE TABLE` 真实 DDL |

枚举列额外生成枚举类到 `core-model/enums`（`@JsonFormat(OBJECT)`，出参为 JSON 对象，入参支持标量或对象）。

## 生成规则要点

- 表强约束：需单列主键（按 PRIMARY KEY 元数据识别，不假设 `id`）+ `create_time` / `update_time`；`create_by` / `update_by` / `del_flag` 为保留审计列，不生成 DO 字段；
- 支持非自增主键（如 varchar 主键）：CreateRequest 自动必填，INSERT 显式携带主键；
- `groupId` 决定基础包名与物理包目录（点号转斜杠，如 `com.jakt` → `com/example`）；`outputDir` 决定工程根目录落点；
- 查询条件：等值 `=`（含 varchar）+ 创建/更新时间区间 + 分页；
- 必填校验：NOT NULL 且无默认值 → `@NotBlank` / `@NotNull`，varchar 附 `@Size(max)`；
- 日志统一 `${toolPrefix}LoggerUtil`，禁止直接 `LoggerFactory`；返回体统一 `${toolPrefix}Result`，Controller 走 `${toolPrefix}Template` 封装。

## 维护约定

- 改生成风格：只改 `templates/table/*.ftl` 或 `skeleton/`，改完跑一次生成 + git diff 评审；
- 新增通用工具类/依赖：加进 `skeleton/`，新项目自动继承；
- 团队统一：本仓库打 tag，各业务项目按固定 tag 生成，避免版本漂移。
