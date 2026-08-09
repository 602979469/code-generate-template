# serviceimpl 包说明

存放对外 RPC 服务接口的实现（Dubbo / gRPC / 自研 RPC）。

约定：

1. RPC 接口定义放在独立的 API 模块（微服务化时拆出）。
2. 实现类只做"协议转换 + 参数校验"，业务逻辑一律委托给 `XxxBizService`。
3. 命名 `XxxRpcServiceImpl`，与接口 `XxxRpcService` 一一对应。

示例：

```
com.jakt.aiplatform.app.biz.serviceimpl/
└── UserRpcServiceImpl   # 实现 UserRpcService，内部调用 UserBizService
```
