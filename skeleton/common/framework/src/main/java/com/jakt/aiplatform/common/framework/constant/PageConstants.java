package com.jakt.aiplatform.common.framework.constant;

/**
 * 分页通用常量：默认值与上限统一收口，禁止业务代码散落魔法值。
 */
public final class PageConstants {

    /** 默认页码。 */
    public static final int DEFAULT_PAGE_NUM = 1;

    /** 默认每页条数。 */
    public static final int DEFAULT_PAGE_SIZE = 10;

    /** 每页条数上限：web 层 {@code @Max} 校验 + DalPageQuery 兜底截断，内部调用也无法全表拉取。 */
    public static final int MAX_PAGE_SIZE = 100;

    private PageConstants() {
    }
}
