package com.jakt.aiplatform.common.dal.query;

import lombok.Data;

/**
 * common-dal 分页查询基类。
 */
@Data
public class DalPageQuery {

    private int pageNum = 1;

    private int pageSize = 10;

    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
}
