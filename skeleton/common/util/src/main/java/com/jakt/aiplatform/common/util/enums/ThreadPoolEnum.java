package com.jakt.aiplatform.common.util.enums;

/**
 * 线程池名称枚举：与 ThreadPoolConfig 中的 Bean 名称一一对应，后续新增线程池在此补充。
 */
public enum ThreadPoolEnum {

    /** 系统业务线程池。 */
    SYS_THREAD_POOL("sysThreadPool"),

    /** 异步任务线程池。 */
    ASYNC_THREAD_POOL("asyncThreadPool"),

    /** 镜像下载任务线程池。 */
    MIRROR_DOWNLOAD("mirrorDownloadThreadPool");

    private final String beanName;

    ThreadPoolEnum(String beanName) {
        this.beanName = beanName;
    }

    /** 对应 Spring Bean 名称。 */
    public String getBeanName() {
        return beanName;
    }
}
