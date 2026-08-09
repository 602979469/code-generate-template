package com.jakt.aiplatform.core.model.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页查询参数基类：页码与每页条数，统一提供 SQL LIMIT 偏移量。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageParam {

    /** 页码，从 1 开始。 */
    private int pageNum = 1;

    /** 每页条数。 */
    private int pageSize = 10;

    /** SQL LIMIT 偏移量。 */
    public int getOffset() {
        return (Math.max(pageNum, 1) - 1) * Math.max(pageSize, 1);
    }
}
