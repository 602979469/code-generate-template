package ${basePackage}.app.web.param;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
<#if hasLocalDateTime>import java.time.LocalDateTime;
</#if><#if hasLocalDate>import java.time.LocalDate;
</#if><#if hasBigDecimal>import java.math.BigDecimal;
</#if>

/**
 * 创建${tableComment}请求 DTO。
 *
 * <p>校验规则与 ${tableName} 表字段对齐：非空 + varchar 长度，不做业务自定义规则。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ${className}CreateRequest extends BaseRequest {
<#list columns as c>
    /** ${c.comment}。 */
<#if c.required && c.string>    @NotBlank(message = "${c.comment}不能为空")
    @Size(max = ${c.length}, message = "${c.comment}长度不能超过 ${c.length}")
    private String ${c.propertyName};
<#elseif c.required>    @NotNull(message = "${c.comment}不能为空")
    private ${c.javaType} ${c.propertyName};
<#else>    private ${c.javaType} ${c.propertyName};
</#if>
</#list>}
