# common-integration 模块

存放外部服务调用集成（HTTP 客户端、RPC 客户端、消息推送等）。

约定：

1. 每个外部服务一个封装类，命名 `XxxClient` 或 `XxxGateway`，放在独立子包（如 `common/integration/ai`）。
2. 外部调用失败统一抛 `BizException(ErrorCodeEnum.EXTERNAL_ERROR, ...)`，禁止吞异常。
3. 调用方（BizService）不直接依赖外部 SDK，只依赖这里的封装，方便替换与测试。
4. 超时、重试、降级策略在封装内部实现，调用方无感知。
5. 需要 HTTP 调用时注入 `common-util` 提供的 `RestTemplate`。

示例（AI 平台可参考）：

```
com/jakt/aiplatform/common/integration/ai/
├── DeepSeekClient        # DeepSeek API 封装
├── XuanYuanClient        # 轩辕 API 封装
└── ChatProperties        # 模型相关配置
```
