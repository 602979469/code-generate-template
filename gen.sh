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
  未提供配置文件时运行 ./gen.sh,可交互生成三个文件:
    generate.yaml          日常模板(generateExample: false,只生成主体/工具类/BaseDO)
    generate-example.yaml  全功能示例配置(generateExample: true 等效配置)
    example.sql            示例表 DDL(example 表,含枚举/json/逻辑删除等全量字段)

示例:
  ./gen.sh ./generate.yaml
EOF
}

# 未提供配置文件：交互式询问是否生成配置模板
if [ $# -eq 0 ]; then
  read -r -p "未提供配置文件,是否在当前目录生成配置模板(generate.yaml + generate-example.yaml + example.sql)? [y/N] " answer
  case "${answer:-N}" in
    y|Y)
      cp "$DIR/generate.yaml.example" ./generate.yaml
      cp "$DIR/generate-example.yaml.example" ./generate-example.yaml
      cp "$DIR/skeleton/sql/example.sql" ./example.sql
      echo "已生成:"
      echo "  ./generate.yaml          日常模板(generateExample: false)"
      echo "  ./generate-example.yaml  全功能示例配置(generateExample: true 等效)"
      echo "  ./example.sql            示例表 DDL(example 表)"
      echo "请编辑后运行: ./gen.sh ./generate.yaml 或 ./gen.sh ./generate-example.yaml"
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
