#!/usr/bin/env bash
set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR="$DIR/generator/target/generator.jar"

usage() {
  cat <<'EOF'
用法:
  ./gen.sh <配置文件>

说明:
  所有配置(项目命名/数据库/tables)都通过 YAML 配置文件提供,不再使用命令行参数。
  未提供配置文件时运行 ./gen.sh,可交互生成两个文件:
    generate.yaml  全配置模板(含 3 张示例表配置:内部表/逻辑删除/全量转换)
    example.sql    示例表 DDL(example_inner / example_logic / example 三张表)

示例:
  ./gen.sh ./generate.yaml
EOF
}

# 未提供配置文件：交互式询问是否生成配置模板
if [ $# -eq 0 ]; then
  read -r -p "未提供配置文件,是否在当前目录生成配置模板(generate.yaml + example.sql)? [y/N] " answer
  case "${answer:-N}" in
    y|Y)
      cp "$DIR/generate.yaml.example" ./generate.yaml
      cp "$DIR/skeleton/sql/example.sql" ./example.sql
      echo "已生成:"
      echo "  ./generate.yaml  全配置模板(含 3 张示例表:内部表/逻辑删除/全量转换)"
      echo "  ./example.sql    示例表 DDL,先执行建表再运行 ./gen.sh ./generate.yaml"
      echo "建表与生成:"
      echo "  mysql -uroot -p < ./example.sql"
      echo "  ./gen.sh ./generate.yaml"
      exit 0
      ;;
    *)
      usage
      exit 1
      ;;
  esac
fi

CONFIG="${1}"
if [ ! -f "$CONFIG" ]; then
  echo "配置文件不存在: $CONFIG"
  usage
  exit 1
fi

if [ ! -f "$JAR" ]; then
  echo "[gen] 首次运行，构建生成器..."
  mvn -q -f "$DIR/generator/pom.xml" -DskipTests package
fi

exec java -Dcgt.templateRepo="$DIR" -jar "$JAR" "$CONFIG"
