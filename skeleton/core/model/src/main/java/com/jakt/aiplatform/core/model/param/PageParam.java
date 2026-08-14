package com.jakt.aiplatform.core.model.param;

import com.jakt.aiplatform.core.model.domain.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分页查询参数基类：继承 {@link com.jakt.aiplatform.core.model.domain.BaseModel} 的公共字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PageParam extends BaseModel {

    /** 页码，从 1 开始。 */
    private int pageNum = 1;

    /** 每页条数。 */
    private int pageSize = 10;

    /**
     * SQL LIMIT 偏移量 = (页码 - 1) * 每页条数。
     *
     * @return 偏移量
     */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }

}
