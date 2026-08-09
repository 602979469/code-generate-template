package com.jakt.aiplatform.core.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 领域模型基类：统一提供 toString/equals/hashCode（Lombok @Data）与序列化能力。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BaseModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键ID。 */
    private Long id;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;
}
