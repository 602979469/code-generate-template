#!/usr/bin/env bash
set -euo pipefail

# code-generate-template 命令行入口
#   初始化新项目: ./gen.sh init -p AiProd -g com.jakt -o ../AiProd
#   新表生成 CRUD: ./gen.sh table -t sys_dept,member -o /path/to/project
#   列出表级模板: ./gen.sh list

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR="$DIR/generator/target/generator.jar"

# 参数校验：无命令或命令不合法时打印用法，不进入 Java
if [ $# -eq 0 ] || { [ "$1" != "init" ] && [ "$1" != "table" ] && [ "$1" != "list" ]; }; then
  echo "用法:"
  echo "  ./gen.sh init  -p AiProd -g com.jakt [-a aiprod] -o 输出目录"
  echo "  ./gen.sh table -t sys_dept,member [-o 目标项目根目录] [-f]"
  echo "  ./gen.sh list"
  exit 1
fi

if [ ! -f "$JAR" ]; then
  echo "[gen] 首次运行，构建生成器..."
  mvn -q -f "$DIR/generator/pom.xml" -DskipTests package
fi

exec java -jar "$JAR" "$@" -c "$DIR/generator.properties"
