package ${basePackage}.web.param;

<#if hasLocalDateTime>import java.time.LocalDateTime;
</#if><#if hasLocalDate>import java.time.LocalDate;
</#if><#if hasBigDecimal>import java.math.BigDecimal;
</#if>
<#if hasString>import jakarta.validation.constraints.Size;
</#if><#if hasRequiredString>import jakarta.validation.constraints.NotBlank;
</#if><#if hasRequiredNonString>import jakarta.validation.constraints.NotNull;
</#if>import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 更新${entityName}请求 DTO。
 *
 * <p>校验规则与 ${tableName} 表字段对齐：非空 + varchar 长度，不做业务自定义规则。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ${className}UpdateRequest extends BaseRequest {
<#list columns as c>
    /** ${c.comment}。 */
<#if c.required && c.string>    @NotBlank(message = "${c.comment}不能为空")
    @Size(max = ${c.length}, message = "${c.comment}长度不能超过 ${c.length}")
    private String ${c.propertyName};
<#elseif c.required>    @NotNull(message = "${c.comment}不能为空")
    private ${c.javaType} ${c.propertyName};
<#elseif c.string>    @Size(max = ${c.length}, message = "${c.comment}长度不能超过 ${c.length}")
    private String ${c.propertyName};
<#else>    private ${c.javaType} ${c.propertyName};
</#if>
</#list>}
