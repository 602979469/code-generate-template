package com.jakt.aiplatform.core.service;

import com.jakt.aiplatform.common.util.tools.AiPlatformInvoker;
import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.enums.ErrorCodeEnum;
import com.jakt.aiplatform.core.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户领域服务：承载用户相关的业务规则（用户名唯一、存在性校验）。
 * 只写规则，不碰持久化细节。
 */
@Service
public class UserDomainService {

    private final UserRepository userRepository;

    public UserDomainService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 创建用户。用户名唯一性是领域规则，由仓储查询保证。
     */
    public User createUser(User user) {
        AiPlatformInvoker.throwErrWhenBlank(user.getUsername(), ErrorCodeEnum.PARAM_INVALID, "用户名不能为空");
        AiPlatformInvoker.throwErrWhenNotNull(userRepository.findByUsername(user.getUsername()), ErrorCodeEnum.USERNAME_EXISTS);
        user.setStatus(user.getStatus() == null ? 0 : user.getStatus());
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        return userRepository.insert(user);
    }

    /**
     * 更新用户。更新后的用户名不得与其他用户冲突。
     */
    public User updateUser(User user) {
        AiPlatformInvoker.throwErrWhenNull(userRepository.findById(user.getId()), ErrorCodeEnum.USER_NOT_FOUND);
        User byName = userRepository.findByUsername(user.getUsername());
        AiPlatformInvoker.throwErrWhenTrue(byName != null && !byName.getId().equals(user.getId()), ErrorCodeEnum.USERNAME_EXISTS);
        user.setUpdateTime(LocalDateTime.now());
        return userRepository.update(user);
    }

    /**
     * 删除用户。用户不存在时抛出业务异常。
     */
    public void deleteUser(Long id) {
        AiPlatformInvoker.throwErrWhenNull(userRepository.findById(id), ErrorCodeEnum.USER_NOT_FOUND);
        userRepository.deleteById(id);
    }

    /**
     * 获取用户。用户不存在时抛出业务异常。
     */
    public User getUser(Long id) {
        User user = userRepository.findById(id);
        AiPlatformInvoker.throwErrWhenNull(user, ErrorCodeEnum.USER_NOT_FOUND);
        return user;
    }
}
