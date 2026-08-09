#!/usr/bin/env bash
set -euo pipefail

# code-generate-template 命令行入口
#   初始化新项目: ./gen.sh init -p AiProd -g com.jakt -o ../AiProd
#   新表生成 CRUD: ./gen.sh table -t sys_dept,member -o /path/to/project

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR="$DIR/generator/target/generator.jar"

if [ ! -f "$JAR" ]; then
  echo "[gen] 首次运行，构建生成器..."
  mvn -q -f "$DIR/generator/pom.xml" -DskipTests package
fi

exec java -jar "$JAR" "$@" -c "$DIR/generator.properties"
