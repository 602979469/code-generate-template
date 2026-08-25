package ${pkgWebParam};

<#if hasLocalDateTime>import java.time.LocalDateTime;
</#if><#if hasLocalDate>import java.time.LocalDate;
</#if><#if hasBigDecimal>import java.math.BigDecimal;
</#if><#if hasString>import jakarta.validation.constraints.Size;
</#if><#if hasRequiredString>import jakarta.validation.constraints.NotBlank;
</#if><#if hasRequiredNonString>import jakarta.validation.constraints.NotNull;
</#if>
${dtoImports}
import ${basePackage}.web.param.BaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 更新${entityName}请求 DTO。
 *
 * <p>校验规则与 ${tableName} 表字段对齐：非空 + varchar 长度，不做业务自定义规则。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ${className}UpdateRequest extends BaseRequest {
<#list columns as c><#if !c.pk>
    /** ${c.comment}。 */
<#if c.required && c.modelString>    @NotBlank(message = "${c.comment}不能为空")
<#if c.length gt 0>    @Size(max = ${c.length}, message = "${c.comment}长度不能超过 ${c.length}")
</#if>    private ${c.modelType} ${c.propertyName};

<#elseif c.required>    @NotNull(message = "${c.comment}不能为空")
    private ${c.modelType} ${c.propertyName};

<#elseif c.modelString><#if c.length gt 0>    @Size(max = ${c.length}, message = "${c.comment}长度不能超过 ${c.length}")
</#if>    private ${c.modelType} ${c.propertyName};

<#else>    private ${c.modelType} ${c.propertyName};

</#if></#if></#list>}
