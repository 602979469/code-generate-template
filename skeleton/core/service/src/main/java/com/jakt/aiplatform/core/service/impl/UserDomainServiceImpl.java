package com.jakt.aiplatform.core.service.impl;

import com.jakt.aiplatform.common.util.tools.AiPlatformInvoker;
import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.enums.ErrorCodeEnum;
import com.jakt.aiplatform.core.model.param.UserQueryParam;
import com.jakt.aiplatform.core.model.result.PageResult;
import com.jakt.aiplatform.core.repository.UserRepository;
import com.jakt.aiplatform.core.service.UserDomainService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户信息表领域服务实现：承载用户信息表相关的业务规则。只写规则，不碰持久化细节。
 */
@Service
public class UserDomainServiceImpl implements UserDomainService {

    /** 用户信息表仓储。 */
    private final UserRepository userRepository;

    public UserDomainServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(User user) {
        AiPlatformInvoker.throwErrWhenBlank(
                user.getLoginName(),
                ErrorCodeEnum.PARAM_INVALID,
                "登录账号不能为空");
        return userRepository.insert(user);
    }

    @Override
    public User updateUser(User user) {
        AiPlatformInvoker.throwErrWhenNull(
                userRepository.findById(user.getId()),
                ErrorCodeEnum.RESOURCE_NOT_FOUND);
        return userRepository.update(user);
    }

    @Override
    public void updateByCondition(User user) {
        AiPlatformInvoker.throwErrWhenNull(
                userRepository.findById(user.getId()),
                ErrorCodeEnum.RESOURCE_NOT_FOUND);
        // 全部业务字段均为空时跳过更新，避免产生空 SQL
        if (user.getDeptId() == null && user.getLoginName() == null && user.getUserName() == null && user.getUserType() == null && user.getEmail() == null && user.getPhonenumber() == null && user.getSex() == null && user.getAvatar() == null && user.getPassword() == null && user.getSalt() == null && user.getStatus() == null && user.getLoginIp() == null && user.getLoginDate() == null && user.getPwdUpdateDate() == null && user.getRemark() == null) {
            return;
        }
        userRepository.updateByCondition(user);
    }

    @Override
    public void deleteUser(Long id) {
        AiPlatformInvoker.throwErrWhenNull(
                userRepository.findById(id),
                ErrorCodeEnum.RESOURCE_NOT_FOUND);
        userRepository.deleteById(id);
    }

    @Override
    public User getUser(Long id) {
        User user = userRepository.findById(id);
        AiPlatformInvoker.throwErrWhenNull(
                user,
                ErrorCodeEnum.RESOURCE_NOT_FOUND);
        return user;
    }

    @Override
    public PageResult<User> findPage(UserQueryParam query) {
        return userRepository.findPage(query);
    }

    @Override
    public List<User> findList(UserQueryParam query) {
        return userRepository.findList(query);
    }
}
