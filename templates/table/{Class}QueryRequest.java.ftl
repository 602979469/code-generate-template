package ${basePackage}.app.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.time.LocalDateTime;
<#if hasLocalDate>import java.time.LocalDate;
</#if>

/**
 * ${tableComment}查询请求。GET 查询参数绑定使用普通类（而非 record），保证 Spring 绑定兼容性。
 */
public class ${className}QueryRequest {

<#list queryColumns as c>
    /** ${c.comment}。 */
    private ${c.javaType} ${c.propertyName};

</#list>
    /** 创建时间起。 */
    private LocalDateTime createTimeBegin;

    /** 创建时间止。 */
    private LocalDateTime createTimeEnd;

    /** 更新时间起。 */
    private LocalDateTime updateTimeBegin;

    /** 更新时间止。 */
    private LocalDateTime updateTimeEnd;

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

</#list>
    public LocalDateTime getCreateTimeBegin() {
        return createTimeBegin;
    }

    public void setCreateTimeBegin(LocalDateTime createTimeBegin) {
        this.createTimeBegin = createTimeBegin;
    }

    public LocalDateTime getCreateTimeEnd() {
        return createTimeEnd;
    }

    public void setCreateTimeEnd(LocalDateTime createTimeEnd) {
        this.createTimeEnd = createTimeEnd;
    }

    public LocalDateTime getUpdateTimeBegin() {
        return updateTimeBegin;
    }

    public void setUpdateTimeBegin(LocalDateTime updateTimeBegin) {
        this.updateTimeBegin = updateTimeBegin;
    }

    public LocalDateTime getUpdateTimeEnd() {
        return updateTimeEnd;
    }

    public void setUpdateTimeEnd(LocalDateTime updateTimeEnd) {
        this.updateTimeEnd = updateTimeEnd;
    }

    public Integer getPageNum() {
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
