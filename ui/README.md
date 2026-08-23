# 独立版代码生成配置页

脱离 RuoYi / Java 后端运行：Node 直连 MySQL 只读 `information_schema`，前端独立页面生成 `generate.yaml`。

## 启动

```bash
npm install   # 只需 mysql2
npm start     # 默认 5180 端口，可用 PORT 覆盖
```

浏览器打开 http://localhost:5180

## 两种模式（URL 参数）

| 模式 | URL | 效果 |
| --- | --- | --- |
| 独立（默认） | `http://localhost:5180/` | 顶部居中显示大字"代码生成器" + 代码主题 logo |
| 内嵌 | `http://localhost:5180/?embed=1`（或 `?mode=embed`） | 无标题头、无 logo，便于以 iframe 等方式内嵌引用 |

> 兼容旧参数：`?mode=standalone` / `?standalone=1` 仍显示 logo（与默认一致）。

## 目录

- `server.js`：Node 服务，`GET /` 返回页面，`POST /schema` 直连数据库读表结构（与 aiplatform SchemaReader 返回结构对齐）
- `index.html`：独立页面（Vue2 + Element UI，本地 vendor 引入，无 CDN 依赖）
- `vendor/`：vue.min.js / element-ui.js / element-ui.css（从 aiplatform-vue node_modules 拷贝）
- `package.json`：唯一依赖 mysql2

## 数据库连接

页面"数据源"填写：主机 / 端口 / 数据库 / 用户名 / 密码（密码可空）。服务只读 `information_schema`，不建表、不改数据。
