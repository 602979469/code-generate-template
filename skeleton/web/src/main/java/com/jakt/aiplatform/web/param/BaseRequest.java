package com.jakt.aiplatform.web.param;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 请求参数基类：统一提供 Lombok 样板，新增请求 DTO 时继承本类。
 */
@Data
public class BaseRequest implements Serializable {

    /** 序列化版本号。 */
    @Serial
    private static final long serialVersionUID = 1L;
}
