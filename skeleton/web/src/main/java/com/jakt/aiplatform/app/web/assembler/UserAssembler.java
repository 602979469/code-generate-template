package com.jakt.aiplatform.app.web.assembler;

import com.jakt.aiplatform.app.web.dto.UserCreateRequest;
import com.jakt.aiplatform.app.web.dto.UserQueryRequest;
import com.jakt.aiplatform.app.web.dto.UserResponse;
import com.jakt.aiplatform.app.web.dto.UserUpdateRequest;
import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.param.UserQueryParam;

/**
 * 用户对象组装器：DTO 与领域模型互转，只存在于 web。
 */
public final class UserAssembler {

    private UserAssembler() {
    }

    public static User toModel(UserCreateRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setNickname(request.nickname());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        return user;
    }

    public static User toModel(UserUpdateRequest request, Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername(request.username());
        user.setNickname(request.nickname());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setStatus(request.status());
        return user;
    }

    public static UserQueryParam toQueryParam(UserQueryRequest request) {
        UserQueryParam param = new UserQueryParam();
        param.setUsername(request.getUsername());
        param.setNickname(request.getNickname());
        param.setStatus(request.getStatus());
        param.setPageNum(request.getPageNum() == null ? 1 : request.getPageNum());
        param.setPageSize(request.getPageSize() == null ? 10 : request.getPageSize());
        return param;
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus(),
                user.getCreateTime(),
                user.getUpdateTime()
        );
    }
}
