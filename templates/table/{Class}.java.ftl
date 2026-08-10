package ${basePackage}.core.model.domain;

<#if hasLocalDateTime>import java.time.LocalDateTime;
</#if><#if hasLocalDate>import java.time.LocalDate;
</#if><#if hasBigDecimal>import java.math.BigDecimal;
</#if>${modelImports}
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ${entityName}领域模型。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ${className} extends BaseModel {
<#list columns as c>
    /** ${c.comment}。 */
    private ${c.modelType} ${c.propertyName};

</#list>}
