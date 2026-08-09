package com.jakt.aiplatform.core.model.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 查询参数基类：所有核心模型查询参数（core.model.param）都继承此类。
 *
 * <p>DO 必有 createTime/updateTime，因此统一提供创建/更新时间的开始-结束区间查询条件，
 * 传了才参与过滤，不传不影响查询。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BaseQueryParam extends PageParam {

    /** 创建时间起。 */
    private LocalDateTime createTimeBegin;

    /** 创建时间止。 */
    private LocalDateTime createTimeEnd;

    /** 更新时间起。 */
    private LocalDateTime updateTimeBegin;

    /** 更新时间止。 */
    private LocalDateTime updateTimeEnd;
}
