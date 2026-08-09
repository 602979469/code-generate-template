package ${basePackage}.web.param;
<#if hasLocalDateTime>import java.time.LocalDateTime;
</#if><#if hasLocalDate>import java.time.LocalDate;
</#if><#if hasBigDecimal>import java.math.BigDecimal;
</#if><#if hasString>import jakarta.validation.constraints.Size;
</#if><#if hasRequiredString>import jakarta.validation.constraints.NotBlank;
</#if><#if hasRequiredNonString>import jakarta.validation.constraints.NotNull;
</#if>import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 创建${entityName}请求 DTO。
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ${className}CreateRequest extends BaseRequest {

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
