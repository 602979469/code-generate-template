package ${basePackage}.app.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
<#if hasLocalDateTime>
import java.time.LocalDateTime;
</#if>
<#if hasLocalDate>
import java.time.LocalDate;
</#if>

/**
 * ${tableComment}查询请求。GET 查询参数绑定使用普通类（而非 record），保证 Spring 绑定兼容性。
 */
public class ${className}QueryRequest {

<#list queryColumns as c>
    /** ${c.comment}。 */
    private ${c.javaType} ${c.propertyName};

</#list>
    /** 页码，从 1 开始。 */
    @Min(value = 1, message = "页码不能小于 1")
    private Integer pageNum = 1;

    /** 每页条数。 */
    @Min(value = 1, message = "每页条数不能小于 1")
    @Max(value = 100, message = "每页条数不能超过 100")
    private Integer pageSize = 10;

<#list queryColumns as c>
    public ${c.javaType} get${c.propertyName?cap_first}() {
        return ${c.propertyName};
    }

    public void set${c.propertyName?cap_first}(${c.javaType} ${c.propertyName}) {
        this.${c.propertyName} = ${c.propertyName};
    }

</#list>    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
