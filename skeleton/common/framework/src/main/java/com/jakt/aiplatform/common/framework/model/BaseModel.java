package com.jakt.aiplatform.common.framework.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 领域模型基类：统一承载审计时间字段。
 * 主键不放在基类，由生成器按数据库 PRIMARY KEY 元数据生成到各 Model。
 */
@Data
public class BaseModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;
}
