package com.jakt.aiplatform.bootstrap;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 应用启动入口。扫描范围覆盖全部模块。
 */
@SpringBootApplication(scanBasePackages = "com.jakt.aiplatform")
@MapperScan("com.jakt.aiplatform.common.dal.mapper")
public class AiPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiPlatformApplication.class, args);
    }
}
