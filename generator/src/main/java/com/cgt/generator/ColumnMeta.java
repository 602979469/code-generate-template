package com.cgt.generator;

/**
 * 表字段元信息。
 */
public final class ColumnMeta {

    public String columnName;
    public String propertyName;
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
}
