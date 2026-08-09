package ${basePackage}.core.model.param;

<#if hasLocalDateTime>import java.time.LocalDateTime;
</#if><#if hasLocalDate>import java.time.LocalDate;
</#if>
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * ${tableComment}查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ${className}QueryParam extends BaseQueryParam {
<#list queryColumns as c><#if c.propertyName != "id">
    /** ${c.comment}。 */
    private ${c.javaType} ${c.propertyName};

</#if></#list>}
