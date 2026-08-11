package ${basePackage}.common.dal.dataobject;

<#if hasLocalDateTime>import java.time.LocalDateTime;
</#if><#if hasLocalDate>import java.time.LocalDate;
</#if><#if hasBigDecimal>import java.math.BigDecimal;
</#if>
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ${entityName} DO对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ${className}DO extends BaseDO {
    /** 主键。 */
    private ${pkJavaType} ${pkPropertyName};

<#list columns as c>
    /** ${c.comment}。 */
    private ${c.javaType} ${c.propertyName};

</#list>}
