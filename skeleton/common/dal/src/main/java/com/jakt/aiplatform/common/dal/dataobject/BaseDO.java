package com.jakt.aiplatform.common.dal.dataobject;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据对象基类：强约束字段。
 *
 * <p>所有表必须包含 id / create_time / update_time 三列，业务字段留在各自 DO 中；
 * 创建者/更新者等审计字段后续由 BizDO extends BaseDO 扩展，本次不处理。
 */
@Data
public class BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 ID。 */
    private Long id;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;

}
