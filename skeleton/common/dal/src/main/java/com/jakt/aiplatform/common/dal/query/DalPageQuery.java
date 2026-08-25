package com.jakt.aiplatform.common.dal.query;

import com.jakt.aiplatform.common.framework.constant.PageConstants;
import lombok.Data;

/**
 * common-dal 分页查询基类：pageSize 上限在 SQL 边界兜底截断，内部调用也无法全表拉取。
 */
@Data
public class DalPageQuery {

    private int pageNum = PageConstants.DEFAULT_PAGE_NUM;

    private int pageSize = PageConstants.DEFAULT_PAGE_SIZE;

    /** 页码（下限 1）。 */
    public int getPageNum() {
        return Math.max(pageNum, PageConstants.DEFAULT_PAGE_NUM);
    }

    /** 每页条数（下限 1、上限 PageConstants.MAX_PAGE_SIZE）。 */
    public int getPageSize() {
        return Math.min(Math.max(pageSize, PageConstants.DEFAULT_PAGE_NUM), PageConstants.MAX_PAGE_SIZE);
    }

    /** SQL LIMIT 偏移量 = (页码 - 1) * 每页条数。 */
    public int getOffset() {
        return (getPageNum() - 1) * getPageSize();
    }
}
