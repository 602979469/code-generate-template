# aiplatform

SOFABoot 风格的多模块 DDD 样板：**每个叶子节点都是一个 Maven 模块**，依赖方向由构建系统强制。

> 结构即约束，规范即文档。把复杂度和规范收进底层模块，业务代码"傻、平、可预测"，AI 才能稳定地生成高质量代码。

## 模块全景

目录按树形结构组织（biz / core / common 是聚合模块，叶子节点是实际模块）；artifactId 与目录名可以不一致。

```
aiplatform（聚合根 pom）
├── bootstrap/                  # aiplatform-bootstrap        启动模块：MainApplication + 扫描 + 配置
├── web/                        # aiplatform-web              视图层：Controller、DTO、ParamChecker、AiPlatformTemplate（统一日志/校验/异常）
├── biz/                        # aiplatform-biz              业务层聚合（空壳）
│   └── service-impl/           # aiplatform-biz-service-impl 业务层：XxxManager（用例编排，操作领域模型）
├── core/                       # aiplatform-core             核心领域层聚合
│   ├── model/                  # aiplatform-core-model       领域模型、查询参数、异常体系（错误码枚举）
│   ├── repository/             # aiplatform-core-repository  仓储层：封装 Mapper，DO → Model
│   └── service/                # aiplatform-core-service     领域服务：XxxService（业务规则）
└── common/                     # aiplatform-common           基础结构层聚合
    ├── dal/                    # aiplatform-common-dal       数据访问：MyBatis Mapper（interface + XML）、DO
    ├── util/                   # aiplatform-common-util      工具：TraceId、RestTemplate、线程池、条件断言（AiPlatformInvoker）
    └── integration/            # aiplatform-common-integration 外部集成：HTTP/RPC 客户端封装（预留）
```

## 依赖方向（红线）

```
web → biz-service-impl → core-service → core-repository → common-dal
                              ↘                      ↗
                         core-model（所有人依赖） common-util / common-integration（基础共享）
```

- `core-model` 零依赖，被所有模块依赖；
- `common-util`、`common-integration` 是基础共享模块，业务模块都依赖；
- 依赖只能从上往下，禁止反向与循环；
- `web` 不直接依赖 `common-dal` / `core-repository`，只通过 `biz-service-impl`。

## 请求流转

```
UserController(web)                        # 参数校验（UserParamChecker）、DTO 转换、Result 包装
  → UserManager/UserManagerImpl(biz)       # 用例编排（接口 + 实现）
  → UserService/UserServiceImpl(core-service) # 领域服务：业务规则（当前示例为纯透传）
      → UserRepository(core-repository)    # 封装 Mapper，DO → Model
        → UserMapper(common-dal)           # MyBatis interface + XML
          → sys_user 表
```

## 分层对象约定

| 层 | 操作的对象 | 说明 |
| --- | --- | --- |
| web | DTO（`XxxRequest`/`XxxResponse`）+ `XxxParamChecker` | 前端相关对象只在这里定义；参数校验集中在 checker |
| biz-service-impl | 领域模型（core-model） | Manager 输入输出都是 Model，不做前端格式转换 |
| core-service | 领域模型 | XxxService 只写业务规则，不碰持久化细节 |
| core-repository | Model（出）/ DO（内部） | 封装 Mapper，DO→Model，可组合多个 Mapper 返回一个 Model |
| common-dal | DO | MyBatis 直接操作对象，与表结构一一对应 |

## 快速开始

1. 初始化数据库（本机 MySQL，root/123456；sql 目录由生成器按 tables 配置每表一个文件）：

   ```bash
   mysql -uroot -p123456 < sql/sys_user.sql
   ```

2. 启动：

   ```bash
   mvn -pl aiplatform-bootstrap -am spring-boot:run
   ```

3. 验证：

   - Swagger：<http://localhost:8080/swagger-ui.html>
   - 分页查询：`GET /api/v1/users/page?pageNum=1&pageSize=10`

## 改造成你的项目

1. 全局替换包名 `com.jakt` → 你的公司域名倒写；
2. 全局替换 `aiplatform` → 你的项目名；
3. 新增业务模块：在 `generate.yaml` 的 `tables` 里配置（`db_table_name` / `model_name` / `model_comment`），运行 code-generate-template 的表级生成器；或让 AI 照着 `User` 模块复制一份。

给 AI 开发代理的指引见 [AGENTS.md](AGENTS.md)。

## 路线图（后期支持，本期不做）

- 纯新手 MVC 单体模板（controller/service/dao/pojo/utils）
- 微服务化：网关、认证、注册中心、配置中心
- 生成器：Maven Archetype → 简易 Web 生成页（start.spring.io 风格）
