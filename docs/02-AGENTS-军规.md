# AGENTS.md — 代码生成器与开发者行为军规

> 版本：v1.0 | 生效日期：2026-08-23 | 适用范围：所有开发人员

---

## 一、模块隔离红线（Maven 依赖级）

| 编号 | 规则 | 违规后果 |
| :--- | :--- | :--- |
| M-001 | core-service / biz-service **不得包含** common-dal 依赖 | 编译不通过 |
| M-002 | core-model **不得依赖** 任何内部模块 | 编译不通过 |
| M-003 | common-framework **不得依赖** 任何 core-/biz- 模块 | 编译不通过 |
| M-004 | common-integration 返回结果 **必须是** core-model 对象 | CR 打回 |

---

## 二、分层职责红线

| 层级 | 禁止事项 |
| :--- | :--- |
| **Controller** | 禁止写 if-else 业务逻辑；禁止直接注入 core-service |
| **Handler** | 禁止写具体业务规则；禁止直接注入 Mapper |
| **DomainService** | 禁止注入 Mapper；禁止写技术性代码 |
| **Repository** | 禁止返回 DO；禁止包含业务逻辑判断 |
| **Model** | 禁止加 Spring/MyBatis 注解；必须是充血模型 |

---

## 三、异常与错误码红线

| 编号 | 规则 |
| :--- | :--- |
| E-001 | 所有业务异常必须使用 `BizException` |
| E-002 | 技术类异常使用 `CommonErrorCode`，业务类异常使用业务域 ErrorCode |
| E-003 | 禁止在全局异常拦截器中对 code 做存在性校验 |

---

## 四、Code Review 必查清单

- [ ] Controller 是否有业务逻辑？
- [ ] Controller 是否直接注入了 core-service？
- [ ] Handler 是否用 `@Transactional` 控制事务？
- [ ] Handler 是否直接注入了 Mapper/Repository？
- [ ] DomainService 是否注入了 Mapper？
- [ ] Repository 是否将 DO 返回给了上层？
- [ ] Model 中是否包含了业务行为方法？
- [ ] Model 中是否有 Spring/MyBatis 注解？
- [ ] 异常是否都使用了 `BizException`？
- [ ] 外部调用是否设置了超时时间？
