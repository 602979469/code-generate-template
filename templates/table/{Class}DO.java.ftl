package ${basePackage}.common.dal.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;
<#if hasLocalDateTime>
import java.time.LocalDateTime;
</#if>
<#if hasLocalDate>
import java.time.LocalDate;
</#if>
<#if hasBigDecimal>
import java.math.BigDecimal;
</#if>

/**
 * ${tableComment}数据对象，与 ${tableName} 表结构一一对应。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ${className}DO extends BaseDO {

<#list columns as c>
    /** ${c.comment}。 */
    private ${c.javaType} ${c.propertyName};

</#list>}
