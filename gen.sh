#!/usr/bin/env bash
set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR="$DIR/generator/target/generator.jar"

usage() {
  cat <<'EOF'
用法:
  init:
    ./gen.sh init -p <项目前缀> -g <groupId> -a <包名后缀> -tp <工具前缀> -o <输出目录>
    示例: ./gen.sh init -p AiProd -g com.jakt -a aiprod -tp AiProd -o ../AiProd

  table:
    ./gen.sh table -t <表名1,表名2> -p <项目前缀> -g <groupId> -a <包名后缀> -tp <工具前缀> [-o <目标项目根目录>] [-f]
    示例: ./gen.sh table -t sys_dept,member -p AiPlatform -g com.jakt -a aiplatform -tp AiPlatform -o .

  list:
    ./gen.sh list

参数说明:
  -p   项目前缀(驼峰,用于启动类名): 如 AiProd
  -g   Maven groupId: 如 com.jakt
  -a   artifactId/包名后缀(小写字母数字,Java 包名不允许连字符): 如 aiprod
  -tp  工具类/异常/常量前缀(驼峰,由你自己指定,代码不做转换): 如 AiProd
  -o   输出目录
  -t   表名,多个用逗号分隔
  -f   覆盖已存在文件(默认跳过)

必填项:
  init  : -p -g -a -tp -o 全部必填,无默认值
  table : -t -p -g -a -tp 必填,-o 默认当前目录
  list  : 无参数
EOF
}

# 命令校验：无命令或命令不合法时打印用法
if [ $# -eq 0 ] || { [ "$1" != "init" ] && [ "$1" != "table" ] && [ "$1" != "list" ]; }; then
  usage
  exit 1
fi

# 必填参数校验：-key 后必须跟一个不以 - 开头的值
has_opt() {
  local key="-${1}"
  local prev=""
  for a in "$@"; do
    if [ "$prev" = "$key" ] && [ "${a#-}" = "$a" ]; then
      return 0
    fi
    prev="$a"
  done
  return 1
}

missing=""
if [ "$1" = "init" ]; then
  for opt in p g a tp o; do
    has_opt "$opt" "$@" || missing="$missing -$opt"
  done
elif [ "$1" = "table" ]; then
  for opt in t p g a tp; do
    has_opt "$opt" "$@" || missing="$missing -$opt"
  done
fi
if [ -n "$missing" ]; then
  echo "缺少必填参数:$missing"
  usage
  exit 1
fi

if [ ! -f "$JAR" ]; then
  echo "[gen] 首次运行，构建生成器..."
  mvn -q -f "$DIR/generator/pom.xml" -DskipTests package
fi

exec java -jar "$JAR" "$@" -c "$DIR/generator.properties"
