package com.jakt.aiplatform.core.model.param;

import com.jakt.aiplatform.core.model.domain.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 分页查询参数基类：继承 {@link com.jakt.aiplatform.core.model.domain.BaseModel} 的公共字段，
 * 统一提供页码、每页条数与 SQL LIMIT 偏移量。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PageParam extends BaseModel {

    /** 页码，从 1 开始。 */
    private int pageNum = 1;

    /** 每页条数。 */
    private int pageSize = 10;

    /** SQL LIMIT 偏移量。 */
    public int getOffset() {
        return (Math.max(pageNum, 1) - 1) * Math.max(pageSize, 1);
    }
}
