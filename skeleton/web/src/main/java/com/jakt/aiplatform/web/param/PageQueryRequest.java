package com.jakt.aiplatform.web.param;

import com.jakt.aiplatform.common.util.constant.PageConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分页查询请求基类：分页查询 DTO 统一继承本类，禁止重复定义 pageNum/pageSize。
 * 分页上限为强约束：web 层 @Min/@Max 校验 + DalPageQuery 兜底截断。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PageQueryRequest extends BaseRequest {

    /** 页码（缺省 PageConstants.DEFAULT_PAGE_NUM）。 */
    @Min(value = PageConstants.DEFAULT_PAGE_NUM, message = "页码不能小于 1")
    private Integer pageNum;

    /** 每页条数（缺省 PageConstants.DEFAULT_PAGE_SIZE）。 */
    @Min(value = PageConstants.DEFAULT_PAGE_NUM, message = "每页条数不能小于 1")
    @Max(value = PageConstants.MAX_PAGE_SIZE, message = "每页条数不能超过 100")
    private Integer pageSize;
}
