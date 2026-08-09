package com.jakt.aiplatform.biz.service.impl;

import com.jakt.aiplatform.biz.service.UserManager;
import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.enums.LogFileEnum;
import com.jakt.aiplatform.core.model.param.UserQueryParam;
import com.jakt.aiplatform.core.model.result.PageResult;
import com.jakt.aiplatform.core.model.util.AiPlatformLoggerUtil;
import com.jakt.aiplatform.core.service.UserDomainService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户信息表 Manager 实现：用例编排，只依赖 core-model 与 core-service（DomainService），不直接触碰仓储。
 */
@Service
public class UserManagerImpl implements UserManager {

    /** 用户信息表领域服务。 */
    private final UserDomainService userDomainService;

    public UserManagerImpl(UserDomainService userDomainService) {
        this.userDomainService = userDomainService;
    }

    @Override
    public User createUser(User user) {
        User created = userDomainService.createUser(user);
        AiPlatformLoggerUtil.info(LogFileEnum.BIZ_SERVICE, "创建用户信息成功 id={}", created.getId());
        return created;
    }

    @Override
    public User getUser(Long id) {
        return userDomainService.getUser(id);
    }

    @Override
    public PageResult<User> pageUsers(UserQueryParam query) {
        return userDomainService.findPage(query);
    }

    @Override
    public List<User> listUsers(UserQueryParam query) {
        return userDomainService.findList(query);
    }

    @Override
    public User updateUser(User user) {
        User updated = userDomainService.updateUser(user);
        AiPlatformLoggerUtil.info(LogFileEnum.BIZ_SERVICE, "更新用户信息成功 id={}", updated.getId());
        return updated;
    }

    @Override
    public void updateByCondition(User user) {
        userDomainService.updateByCondition(user);
        AiPlatformLoggerUtil.info(LogFileEnum.BIZ_SERVICE, "按条件更新用户信息成功 id={}", user.getId());
    }

    @Override
    public void deleteUser(Long id) {
        userDomainService.deleteUser(id);
        AiPlatformLoggerUtil.info(LogFileEnum.BIZ_SERVICE, "删除用户信息成功 id={}", id);
    }
}
