package ${basePackage}.app.web.dto;

import java.time.LocalDateTime;

/**
 * ${tableComment}响应 DTO。
 */
public record ${className}Response(
        /** 主键 ID。 */
        Long id,
<#list columns as c>        /** ${c.comment}。 */
        ${c.javaType} ${c.propertyName},
</#list>        /** 创建时间。 */
        LocalDateTime createTime,
        /** 更新时间。 */
        LocalDateTime updateTime
) {
}
