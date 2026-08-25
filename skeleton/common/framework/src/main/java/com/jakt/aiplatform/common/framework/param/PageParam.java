package com.jakt.aiplatform.common.framework.param;

import com.jakt.aiplatform.common.framework.model.BaseModel;
import com.jakt.aiplatform.common.util.constant.PageConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 分页查询参数基类：继承 {@link com.jakt.aiplatform.common.framework.model.BaseModel} 的公共字段
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PageParam extends BaseModel {

    /** 页码（缺省 PageConstants.DEFAULT_PAGE_NUM）。 */
    private int pageNum = PageConstants.DEFAULT_PAGE_NUM;

    /** 每页条数（缺省 PageConstants.DEFAULT_PAGE_SIZE）。 */
    private int pageSize = PageConstants.DEFAULT_PAGE_SIZE;

    /** 页码（下限 1）。 */
    public int getPageNum() {
        return Math.max(pageNum, PageConstants.DEFAULT_PAGE_NUM);
    }

    /** 每页条数（下限 1、上限 PageConstants.MAX_PAGE_SIZE）。 */
    public int getPageSize() {
        return Math.min(Math.max(pageSize, PageConstants.DEFAULT_PAGE_NUM), PageConstants.MAX_PAGE_SIZE);
    }

    /**
     * SQL LIMIT 偏移量 = (页码 - 1) * 每页条数。
     *
     * @return 偏移量
     */
    public int getOffset() {
        return (getPageNum() - 1) * getPageSize();
    }

}
