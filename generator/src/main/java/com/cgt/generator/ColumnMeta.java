package com.cgt.generator;

/**
 * 表字段元信息。
 */
public final class ColumnMeta {

    public String columnName;
    public String propertyName;
    /** 数据库原始类型（json / varchar / bigint …），用于 json 语义校验。 */
    public String dbType;
    public String javaType;
    public String comment;
    public boolean pk;
    public boolean auto;
    /** NOT NULL 且无默认值，生成必填校验。 */
    public boolean required;
    /** LIKE / EQ / NONE（NONE 不进入查询条件）。 */
    public String queryType;
    /** 字段长度（varchar 等），用于 DTO @Size。 */
    public int length;
    public boolean string;

    /** Model/DTO 层类型（默认与 javaType 一致；枚举/jsonArray/强制转换后不同）。 */
    public String modelType;
    /** 是否枚举列。 */
    public boolean enumColumn;
    /** 枚举类名（enumColumn 时）。 */
    public String enumClassName;
    /** 转换策略：NONE / ENUM / JSON / JSON_ARRAY / JSON_OBJECT / COERCE。 */
    public String conversion = "NONE";
    /** 是否敏感列：生成时从查询参数/响应/查询条件中剔除（默认按列名识别，可配置）。 */
    public boolean sensitive;
    /** 脱敏策略（列级）：PHONE / ID_CARD / BANK_CARD / EMAIL / NAME / ADDRESS / PASSWORD / NONE。 */
    public String sensitiveStrategy;
    /** jsonArray 元素类型 / jsonObject 目标类型（全限定名或泛型）。 */
    public String jsonElementType;
    /** 枚举 code 类型（Integer/String/Long）。 */
    public String enumCodeType;
    /** 枚举值（Freemarker 渲染枚举模板用）。 */
    public java.util.List<GeneratorConfig.EnumValue> enumValues = new java.util.ArrayList<>();
    /** DO → Model 的转换表达式（右值）。 */
    public String toModelExpr;
    /** Model → DO 的转换表达式（右值）。 */
    public String toDoExpr;
    /** QueryParam → DalQuery 的转换表达式（右值，{query} 为 QueryParam 变量名）。 */
    public String toDalExpr;
    /** Model 类型是否为 String（决定 @Size/@NotBlank 与导入）。 */
    public boolean modelString;

    public String getColumnName() {
        return columnName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getJavaType() {
        return javaType;
    }

    public String getComment() {
        return comment;
    }

    public String getQueryType() {
        return queryType;
    }

    public int getLength() {
        return length;
    }

    public boolean isString() {
        return string;
    }

    public boolean isRequired() {
        return required;
    }

    public String getModelType() {
        return modelType;
    }

    public boolean isEnumColumn() {
        return enumColumn;
    }

    public String getConversion() {
        return conversion;
    }

    public String getJsonElementType() {
        return jsonElementType;
    }

    public String getToModelExpr() {
        return toModelExpr;
    }

    public String getToDoExpr() {
        return toDoExpr;
    }

    public String getToDalExpr() {
        return toDalExpr;
    }

    public boolean isSensitive() {
        return sensitive;
    }

    public String getSensitiveStrategy() {
        return sensitiveStrategy;
    }

    public boolean isModelString() {
        return modelString;
    }

    public boolean isPk() {
        return pk;
    }

    public String getEnumClassName() {
        return enumClassName;
    }

    public String getEnumCodeType() {
        return enumCodeType;
    }

    public java.util.List<GeneratorConfig.EnumValue> getEnumValues() {
        return enumValues;
    }
}
