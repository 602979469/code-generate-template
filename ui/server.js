#!/usr/bin/env node
/**
 * 独立版代码生成配置服务：
 * - GET  /        返回页面 index.html
 * - POST /schema  直连 MySQL 读 information_schema，返回与 aiplatform SchemaReader 对齐的结构
 *
 * 启动：node server.js（默认 5180 端口，可用 PORT 环境变量覆盖）
 */
const http = require('http')
const fs = require('fs')
const path = require('path')
const os = require('os')
const { execFile, spawnSync } = require('child_process')
const mysql = require('mysql2/promise')

const PORT = Number(process.env.PORT || 5180)
const TEMPLATE_ROOT = path.join(__dirname, '..')
const SENSITIVE = new Set(['password', 'pwd', 'token', 'api_key', 'apikey', 'secret', 'salt'])

function mapJavaType(type) {
  switch (type) {
    case 'bigint': return 'Long'
    case 'int': case 'integer': case 'smallint': case 'mediumint': case 'tinyint': return 'Integer'
    case 'varchar': case 'char': case 'text': case 'longtext': case 'mediumtext': case 'tinytext': return 'String'
    case 'datetime': case 'timestamp': return 'LocalDateTime'
    case 'date': return 'LocalDate'
    case 'decimal': case 'numeric': return 'BigDecimal'
    case 'double': case 'float': return 'Double'
    case 'bit': case 'boolean': case 'bool': return 'Boolean'
    default: return 'String'
  }
}

function supportedTypes(javaType) {
  const list = [javaType]
  switch (javaType) {
    case 'String':
      list.push('Integer', 'Long', 'BigDecimal', 'Double', 'Float', 'Short', 'Byte', 'Boolean')
      break
    case 'Integer': case 'Long': case 'BigDecimal': case 'Double': case 'Float': case 'Short': case 'Byte':
      list.push('String')
      break
    case 'Boolean':
      list.push('String')
      break
  }
  return list
}

function toCamel(name) {
  return name.replace(/_([a-z])/g, (_, ch) => ch.toUpperCase())
}

function pascal(name) {
  const camel = toCamel(name)
  return camel ? camel.charAt(0).toUpperCase() + camel.slice(1) : name
}

function parseEnum(columnType) {
  const m = /^enum\((.*)\)$/i.exec(columnType || '')
  if (!m) return []
  return (m[1].match(/'((?:[^']|'')*)'/g) || []).map(v => v.replace(/^'|'$/g, '').replace(/''/g, "'"))
}

/** information_schema 返回大写列名，统一转小写键。 */
function normalizeRow(row) {
  const out = {}
  Object.keys(row).forEach(key => {
    out[key.toLowerCase()] = row[key]
  })
  return out
}

function toColumn(c) {
  const dbType = c.data_type
  const javaType = mapJavaType(dbType)
  const rawLength = Number(c.character_maximum_length)
  const pk = c.column_key === 'PRI'
  const auto = (c.extra || '').includes('auto_increment')
  const required = !pk && !auto && c.is_nullable === 'NO' && c.column_default == null
  return {
    columnName: c.column_name,
    propertyName: toCamel(c.column_name),
    dbType,
    javaType,
    comment: c.column_comment || c.column_name,
    pk,
    auto,
    required,
    length: rawLength > 2147483647 ? 0 : rawLength,
    string: javaType === 'String',
    sensitive: SENSITIVE.has(String(c.column_name).toLowerCase()),
    queryType: 'EQ',
    supportedTypes: supportedTypes(javaType),
    enumNativeValues: dbType === 'enum' ? parseEnum(c.column_type) : []
  }
}

function buildSuggest(tableName, comment, columns) {
  const suggest = {
    modelName: pascal(tableName),
    modelComment: comment || tableName
  }
  const del = columns.find(c => c.columnName === 'del_flag')
  suggest.logicDelete = del
    ? { enable: true, columnName: 'del_flag', normalValue: '0', deleteValue: '1' }
    : null
  return suggest
}

async function readSchema(payload) {
  const conn = await mysql.createConnection({
    host: payload.host,
    port: Number(payload.port || 3306),
    user: payload.username,
    password: payload.password || '',
    connectTimeout: 8000
  })
  try {
    const [tables] = await conn.query(
      'SELECT table_name, table_comment FROM information_schema.tables WHERE table_schema = ? ORDER BY table_name',
      [payload.database]
    )
    const [cols] = await conn.query(
      'SELECT table_name, column_name, data_type, character_maximum_length, column_comment, is_nullable, ' +
      'column_default, column_key, extra, column_type FROM information_schema.columns ' +
      'WHERE table_schema = ? ORDER BY table_name, ordinal_position',
      [payload.database]
    )
    const tableRows = tables.map(normalizeRow)
    const colRows = cols.map(normalizeRow)
    const byTable = {}
    colRows.forEach(c => {
      ;(byTable[c.table_name] = byTable[c.table_name] || []).push(c)
    })
    const warnings = []
    const list = tableRows.map(t => {
      const columns = (byTable[t.table_name] || []).map(toColumn)
      if (!columns.some(c => c.pk)) {
        warnings.push({ tableName: t.table_name, message: '缺少主键' })
      }
      const hasTime = ['create_time', 'update_time'].every(name => columns.some(c => c.columnName === name))
      if (!hasTime) {
        warnings.push({ tableName: t.table_name, message: '缺少 create_time / update_time 审计时间列' })
      }
      return {
        tableName: t.table_name,
        comment: t.table_comment || '',
        columns,
        suggest: buildSuggest(t.table_name, t.table_comment || '', columns)
      }
    })
    return { tables: list, warnings }
  } finally {
    await conn.end().catch(() => {})
  }
}

function sendJson(res, status, body) {
  res.writeHead(status, { 'Content-Type': 'application/json; charset=utf-8' })
  res.end(JSON.stringify(body))
}

/**
 * 调用 code-generate-template 生成器。
 * packageMode=false（本地模式）：按 yaml 的 outputDir 直接生成到指定路径；
 * packageMode=true（集群模式）：临时目录生成 + tar.gz 打包，忽略 yaml 的 outputDir。
 */
function runGenerator(yamlContent, packageMode) {
  return new Promise(resolve => {
    const tmpDir = fs.mkdtempSync(path.join(os.tmpdir(), 'codegen-'))
    const projectDir = path.join(tmpDir, 'project')
    const archivePath = path.join(tmpDir, 'codegen-project.tar.gz')
    // 集群模式：覆盖 outputDir 为临时目录；本地模式：保持 yaml 原样
    const yamlPatched = packageMode
      ? String(yamlContent).replace(/^(outputDir\s*:).*$/m, 'outputDir: ' + projectDir)
      : String(yamlContent)
    if (packageMode) {
      fs.mkdirSync(projectDir, { recursive: true })
    }
    const yamlPath = path.join(tmpDir, 'generate.yaml')
    fs.writeFileSync(yamlPath, yamlPatched, 'utf8')
    const cleanup = () => {
      try { fs.rmSync(tmpDir, { recursive: true, force: true }) } catch (e) {}
    }
    const jar = path.join(TEMPLATE_ROOT, 'generator', 'target', 'generator.jar')
    if (!fs.existsSync(jar)) {
      const build = spawnSync('mvn', ['-q', '-f', path.join(TEMPLATE_ROOT, 'generator', 'pom.xml'), '-DskipTests', 'package'],
        { stdio: 'pipe', timeout: 300000 })
      if (build.status !== 0) {
        cleanup()
        return resolve({ success: false, log: (build.stderr || '').toString(), errorMessage: '生成器构建失败' })
      }
    }
    execFile('java', ['-Dcgt.templateRepo=' + TEMPLATE_ROOT, '-jar', jar, yamlPath], {
      cwd: TEMPLATE_ROOT,
      timeout: 300000,
      maxBuffer: 10 * 1024 * 1024
    }, (err, stdout, stderr) => {
      const log = ((stdout || '') + '\n' + (stderr || '')).trim()
      if (err) {
        cleanup()
        return resolve({ success: false, log, errorMessage: err.message || '生成失败' })
      }
      if (!packageMode) {
        cleanup()
        return resolve({ success: true, log })
      }
      execFile('tar', ['-czf', archivePath, '-C', projectDir, '.'], tarErr => {
        if (tarErr) {
          cleanup()
          return resolve({ success: false, log: log + '\n打包失败: ' + tarErr.message, errorMessage: '打包失败' })
        }
        resolve({ success: true, log, archivePath, cleanup })
      })
    })
  })
}

const server = http.createServer(async (req, res) => {
  const pathname = new URL(req.url, 'http://localhost').pathname
  if (req.method === 'GET' && (pathname === '/' || pathname === '/index.html')) {
    const file = path.join(__dirname, 'index.html')
    res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' })
    fs.createReadStream(file).pipe(res)
    return
  }
  if (req.method === 'GET' && pathname.startsWith('/vendor/')) {
    const file = path.join(__dirname, pathname)
    const type = path.extname(file) === '.css'
      ? 'text/css; charset=utf-8'
      : 'application/javascript; charset=utf-8'
    if (!fs.existsSync(file)) {
      return sendJson(res, 404, { success: false, errorCode: 'NOT_FOUND', errorMessage: 'Not Found' })
    }
    res.writeHead(200, { 'Content-Type': type })
    fs.createReadStream(file).pipe(res)
    return
  }
  if (req.method === 'POST' && pathname === '/schema') {
    let raw = ''
    req.on('data', chunk => { raw += chunk })
    req.on('end', async () => {
      let body = {}
      try {
        body = JSON.parse(raw || '{}')
      } catch (e) {
        return sendJson(res, 400, { success: false, errorCode: 'PARAM_INVALID', errorMessage: '请求体不是合法 JSON' })
      }
      const { host, port, database, username, password } = body
      if (!host || !database || !username) {
        return sendJson(res, 400, {
          success: false,
          errorCode: 'PARAM_INVALID',
          errorMessage: '请填写完整数据库连接信息：主机、端口、数据库、用户名（密码可为空）'
        })
      }
      try {
        const data = await readSchema({ host, port, database, username, password })
        return sendJson(res, 200, { success: true, data })
      } catch (e) {
        const message = (e && e.message) || '数据库连接失败'
        return sendJson(res, 400, { success: false, errorCode: 'DB_ERROR', errorMessage: message })
      }
    })
    return
  }
  if (req.method === 'POST' && pathname === '/generate') {
    let raw = ''
    req.on('data', chunk => { raw += chunk })
    req.on('end', async () => {
      let body = {}
      try {
        body = JSON.parse(raw || '{}')
      } catch (e) {
        return sendJson(res, 400, { success: false, errorCode: 'PARAM_INVALID', errorMessage: '请求体不是合法 JSON' })
      }
      const yaml = body.yaml || ''
      if (!yaml.trim()) {
        return sendJson(res, 400, { success: false, errorCode: 'PARAM_INVALID', errorMessage: '缺少 generate.yaml 内容' })
      }
      const packageMode = body.package === true
      const result = await runGenerator(yaml, packageMode)
      if (!result.success) {
        return sendJson(res, 400, { success: false, log: result.log, errorMessage: result.errorMessage })
      }
      if (!packageMode) {
        return sendJson(res, 200, { success: true, log: result.log })
      }
      // 成功：直接返回打包好的 tar.gz 压缩包
      res.writeHead(200, {
        'Content-Type': 'application/gzip',
        'Content-Disposition': 'attachment; filename="codegen-project.tar.gz"'
      })
      fs.createReadStream(result.archivePath).pipe(res).on('close', result.cleanup)
    })
    return
  }
  sendJson(res, 404, { success: false, errorCode: 'NOT_FOUND', errorMessage: 'Not Found' })
})

server.listen(PORT, () => {
  console.log(`codegen-ui standalone 已启动: http://localhost:${PORT}`)
})
