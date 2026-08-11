package com.jakt.aiplatform.web.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 响应 DTO 基类：统一承载审计时间字段与 Lombok 样板，新增响应 DTO 时继承本类。
 * 主键不放在基类，由生成器按数据库 PRIMARY KEY 元数据生成到各 Response。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResult implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;
}
