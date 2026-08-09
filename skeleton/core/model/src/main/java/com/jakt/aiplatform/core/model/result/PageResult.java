package com.jakt.aiplatform.core.model.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 分页结果容器（core-model 定义，与持久化框架无关）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageResult<T> {

    /** 总条数。 */
    private long total;

    /** 页码，从 1 开始。 */
    private int pageNum = 1;

    /** 每页条数。 */
    private int pageSize = 10;

    /** 当前页数据列表。 */
    private List<T> dataList;
}
