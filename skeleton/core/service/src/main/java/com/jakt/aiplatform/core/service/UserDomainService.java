package com.jakt.aiplatform.core.service;

import com.jakt.aiplatform.common.util.tools.AiPlatformInvoker;
import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.enums.ErrorCodeEnum;
import com.jakt.aiplatform.core.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户信息表领域服务：承载用户信息表相关的业务规则。只写规则，不碰持久化细节。
 */
@Service
public class UserDomainService {

    /** 用户信息表仓储。 */
    private final UserRepository userRepository;

    public UserDomainService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        AiPlatformInvoker.throwErrWhenBlank(user.getLoginName(), ErrorCodeEnum.PARAM_INVALID, "登录账号不能为空");
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        return userRepository.insert(user);
    }

    public User updateUser(User user) {
        AiPlatformInvoker.throwErrWhenNull(userRepository.findById(user.getId()), ErrorCodeEnum.RESOURCE_NOT_FOUND);
        user.setUpdateTime(LocalDateTime.now());
        return userRepository.update(user);
    }

    public void deleteUser(Long id) {
        AiPlatformInvoker.throwErrWhenNull(userRepository.findById(id), ErrorCodeEnum.RESOURCE_NOT_FOUND);
        userRepository.deleteById(id);
    }

    public User getUser(Long id) {
        User user = userRepository.findById(id);
        AiPlatformInvoker.throwErrWhenNull(user, ErrorCodeEnum.RESOURCE_NOT_FOUND);
        return user;
    }
}
