package ${basePackage}.web.result;

<#if hasLocalDateTime>import java.time.LocalDateTime;
</#if><#if hasLocalDate>import java.time.LocalDate;
</#if><#if hasBigDecimal>import java.math.BigDecimal;
</#if>${dtoImports}
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * ${entityName}响应 DTO。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ${className}Response extends BaseResult {
<#if !compositePk>
    /** 主键。 */
    private ${pkJavaType} ${pkPropertyName};

</#if>
<#list columns as c><#if !c.sensitive>
    /** ${c.comment}。 */
    private ${c.modelType} ${c.propertyName};

</#if></#list>}
