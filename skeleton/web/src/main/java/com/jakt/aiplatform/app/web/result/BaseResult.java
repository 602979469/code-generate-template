package com.jakt.aiplatform.app.web.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 响应 DTO 基类：统一承载主键/创建/更新时间与 Lombok 样板，新增响应 DTO 时继承本类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResult implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 ID。 */
    private Long id;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;
}
