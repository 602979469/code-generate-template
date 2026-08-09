package ${basePackage}.app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
 * 更新${tableComment}请求 DTO。
 */
public record ${className}UpdateRequest(
<#list columns as c>
        <#if c.required && c.string>
        @NotBlank(message = "${c.comment}不能为空")
        @Size(max = ${c.length}, message = "${c.comment}长度不能超过 ${c.length}")
        String ${c.propertyName}
        <#elseif c.required>
        @NotNull(message = "${c.comment}不能为空")
        ${c.javaType} ${c.propertyName}
        <#else>
        ${c.javaType} ${c.propertyName}
        </#if><#sep>,</#sep>
</#list>
) {
}
