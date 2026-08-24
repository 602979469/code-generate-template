package com.jakt.aiplatform.common.dal.dataobject;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DO 对象基类：统一承载审计时间字段。
 * 主键不放在基类，由生成器按数据库 PRIMARY KEY 元数据生成到各 DO。
 */
@Data
public class BaseDO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;

}
