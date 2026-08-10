package ${basePackage}.core.model.param;

import java.time.LocalDateTime;
<#if hasLocalDate>import java.time.LocalDate;
</#if><#if hasBigDecimal>import java.math.BigDecimal;
</#if>import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * ${entityName}查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ${className}QueryParam extends PageParam {

<#list queryColumns as c><#if c.propertyName != "id">
    /** ${c.comment}。 */
    private ${c.javaType} ${c.propertyName};

</#if></#list>
    /** 创建时间起。 */
    private LocalDateTime createTimeBegin;

    /** 创建时间止。 */
    private LocalDateTime createTimeEnd;

    /** 更新时间起。 */
    private LocalDateTime updateTimeBegin;

    /** 更新时间止。 */
    private LocalDateTime updateTimeEnd;

}
