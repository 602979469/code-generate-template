package com.jakt.aiplatform.app.web.dto;

import java.time.LocalDateTime;

/**
 * 用户响应 DTO。
 */
public record UserResponse(
        Long id,
        String username,
        String nickname,
        String email,
        String phone,
        Integer status,
        LocalDateTime createTime,
        LocalDateTime updateTime
) {
}
