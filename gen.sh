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
  echo "  ./gen.sh init  -p 项目前缀 -g groupId [-a artifact后缀] -o 输出目录"
  echo "  ./gen.sh table -t 表名1,表名2 [-p 项目前缀] [-g groupId] [-a artifact后缀] [-o 目标项目] [-f]"
  echo "  ./gen.sh list"
  echo ""
  echo "示例:"
  echo "  ./gen.sh init  -p AiProd -g com.jakt -a aiprod -o ../AiProd"
  echo "  ./gen.sh table -t sys_dept -o /Users/jakt/IdeaProjects/aiplatform"
  echo ""
  echo "说明:"
  echo "  -p/-g/-a 指定项目命名(类名前缀/groupId/artifactId后缀)。"
  echo "  不传时: init 用 generator.properties 默认值; table 会自动从目标项目"
  echo "  识别项目名(如 AiPlatform/aiplatform)，识别失败才用默认值。"
  exit 1
fi

if [ ! -f "$JAR" ]; then
  echo "[gen] 首次运行，构建生成器..."
  mvn -q -f "$DIR/generator/pom.xml" -DskipTests package
fi

exec java -jar "$JAR" "$@" -c "$DIR/generator.properties"
