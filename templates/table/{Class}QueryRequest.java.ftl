package ${pkgWebParam};

import java.time.LocalDateTime;
<#if hasLocalDate>import java.time.LocalDate;
</#if><#if hasBigDecimal>import java.math.BigDecimal;
</#if>${modelImports}import ${basePackage}.web.param.PageQueryRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ${entityName}查询请求。分页参数由 {@link PageQueryRequest} 提供。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ${className}QueryRequest extends PageQueryRequest {

<#list queryColumns as c>
    /** ${c.comment}。 */
    private ${c.modelType} ${c.propertyName};

</#list>    /** 创建时间起。 */
    private LocalDateTime createTimeBegin;

    /** 创建时间止。 */
    private LocalDateTime createTimeEnd;

    /** 更新时间起。 */
    private LocalDateTime updateTimeBegin;

    /** 更新时间止。 */
    private LocalDateTime updateTimeEnd;
}
