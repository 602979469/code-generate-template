#!/usr/bin/env bash
set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR="$DIR/generator/target/generator.jar"

usage() {
  cat <<'EOF'
用法:
  ./gen.sh <配置文件>               按配置文件初始化项目并生成表级 CRUD
  ./gen.sh --validate <配置文件>    校验模式：只检查配置/表结构/生成计划，不落盘

说明:
  所有配置(项目命名/数据库/tables)都通过 YAML 配置文件提供,不再使用命令行参数。
  未提供配置文件时运行 ./gen.sh,可交互生成两个文件:
    generate.yaml  全配置模板(含 3 张示例表配置:内部表/逻辑删除/全量转换)
    example.sql    示例表 DDL(example_inner / example_logic / example 三张表)
  改完配置不确定时,先用校验模式看生成计划,确认无误再正式生成:
    ./gen.sh --validate ./generate.yaml

示例:
  ./gen.sh ./generate.yaml
  ./gen.sh --validate ./generate.yaml
EOF
}

# 参数解析：--validate / --dry-run 进入校验模式
MODE="gen"
CONFIG=""
if [ $# -ge 1 ] && { [ "$1" = "--validate" ] || [ "$1" = "--dry-run" ]; }; then
  MODE="validate"
  shift
  CONFIG="${1:-}"
  if [ -z "$CONFIG" ]; then
    echo "校验模式需要配置文件: ./gen.sh --validate <配置文件>"
    usage
    exit 1
  fi
else
  CONFIG="${1:-}"
fi

# 未提供配置文件：交互式询问是否生成配置模板
if [ "$MODE" = "gen" ] && [ $# -eq 0 ]; then
  read -r -p "未提供配置文件,是否在当前目录生成配置模板(generate.yaml + example.sql)? [y/N] " answer
  case "${answer:-N}" in
    y|Y)
      if [ -f ./generate.yaml ]; then
        echo "generate.yaml 已存在，为避免覆盖已定制配置，跳过生成模板。"
        echo "如需重新生成模板，请先删除或改名 ./generate.yaml 后重试。"
      else
        cp "$DIR/generate.yaml.example" ./generate.yaml
        echo "已生成 ./generate.yaml 全配置模板(含 3 张示例表:内部表/逻辑删除/全量转换)"
      fi
      cp "$DIR/skeleton/sql/example.sql" ./example.sql
      echo "已生成 ./example.sql 示例表 DDL"
      echo "建表与生成:"
      echo "  mysql -uroot -p < ./example.sql"
      echo "  ./gen.sh ./generate.yaml"
      echo "先校验再生成(推荐):"
      echo "  ./gen.sh --validate ./generate.yaml"
      exit 0
      ;;
    *)
      usage
      exit 1
      ;;
  esac
fi

if [ ! -f "$CONFIG" ]; then
  echo "配置文件不存在: $CONFIG"
  usage
  exit 1
fi

if [ ! -f "$JAR" ]; then
  echo "[gen] 首次运行，构建生成器..."
  mvn -q -f "$DIR/generator/pom.xml" -DskipTests package
fi

if [ "$MODE" = "validate" ]; then
  exec java -Dcgt.templateRepo="$DIR" -jar "$JAR" --validate "$CONFIG"
else
  exec java -Dcgt.templateRepo="$DIR" -jar "$JAR" "$CONFIG"
fi
