package com.jakt.aiplatform.common.util.enums;

import lombok.Getter;

/**
 * 日志文件枚举：与日志配置中的 Logger/文件名称对应，新增日志文件在此登记。
 */
@Getter
public enum LogFileEnum {

    /** 通用错误日志。 */
    COMMON_ERROR("common-error"),

    /** 业务服务日志。 */
    BIZ_SERVICE("biz-service"),

    /** 异步调度日志。 */
    ASYNC_SCHEDULE("async-schedule");

    /** 日志文件名 / Logger 名称。 */
    private final String fileName;

    LogFileEnum(String fileName) {
        this.fileName = fileName;
    }
}
