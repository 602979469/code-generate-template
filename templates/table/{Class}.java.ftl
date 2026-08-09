package ${basePackage}.core.model.domain;

import lombok.Data;
<#if hasLocalDateTime>import java.time.LocalDateTime;
</#if><#if hasLocalDate>import java.time.LocalDate;
</#if><#if hasBigDecimal>import java.math.BigDecimal;
</#if>

/**
 * ${tableComment}领域模型。
 */
@Data
public class ${className} extends BaseModel {
<#list columns as c>
    /** ${c.comment}。 */
    private ${c.javaType} ${c.propertyName};

</#list>}
