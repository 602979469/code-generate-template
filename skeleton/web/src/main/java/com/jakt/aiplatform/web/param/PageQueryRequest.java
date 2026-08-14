package com.jakt.aiplatform.web.param;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分页查询请求基类：分页查询 DTO 统一继承本类，禁止重复定义 pageNum/pageSize。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PageQueryRequest extends BaseRequest {

    /** 页码（缺省走 PageParam 默认值 1）。 */
    private Integer pageNum;

    /** 每页条数（缺省走 PageParam 默认值 10）。 */
    private Integer pageSize;
}
