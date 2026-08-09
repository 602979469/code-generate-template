package ${basePackage}.app.web.dto;

import java.time.LocalDateTime;

/**
 * ${tableComment}响应 DTO。
 */
public record ${className}Response(
        Long id,
<#list columns as c>
        ${c.javaType} ${c.propertyName}<#sep>,</#sep>
</#list>,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
}
