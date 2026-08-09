package com.jakt.aiplatform.common.util.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置：全局注入带超时设置的 RestTemplate，供外部集成使用。
 */
@Configuration
public class RestTemplateConfig {

    private static final int CONNECT_TIMEOUT_MILLIS = 5_000;

    private static final int READ_TIMEOUT_MILLIS = 10_000;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
        factory.setReadTimeout(READ_TIMEOUT_MILLIS);
        return new RestTemplate(factory);
    }
}
