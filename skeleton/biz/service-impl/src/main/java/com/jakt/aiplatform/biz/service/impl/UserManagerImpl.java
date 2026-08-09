package com.jakt.aiplatform.biz.service.impl;

import com.jakt.aiplatform.biz.service.UserManager;
import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.enums.LogFileEnum;
import com.jakt.aiplatform.core.model.param.UserQueryParam;
import com.jakt.aiplatform.core.model.result.PageResult;
import com.jakt.aiplatform.core.model.util.AiPlatformLoggerUtil;
import com.jakt.aiplatform.core.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户管理实现类
 *
 */
@Service
public class UserManagerImpl implements UserManager {

    /** 用户领域服务。 */
    private final UserService userService;

    public UserManagerImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public User createUser(User user) {
        User created = userService.createUser(user);
        AiPlatformLoggerUtil.info(LogFileEnum.BIZ_SERVICE, "创建用户成功 id={}", created.getId());
        return created;
    }

    @Override
    public User getUser(Long id) {
        return userService.getUser(id);
    }

    @Override
    public PageResult<User> pageUsers(UserQueryParam query) {
        return userService.findPage(query);
    }

    @Override
    public List<User> listUsers(UserQueryParam query) {
        return userService.findList(query);
    }

    @Override
    public void updateUser(User user) {
        userService.updateUser(user);
        AiPlatformLoggerUtil.info(LogFileEnum.BIZ_SERVICE, "更新用户成功 id={}", user.getId());
    }

    @Override
    public void updateByCondition(User user) {
        userService.updateByCondition(user);
        AiPlatformLoggerUtil.info(LogFileEnum.BIZ_SERVICE, "按条件更新用户成功 id={}", user.getId());
    }

    @Override
    public void deleteUser(Long id) {
        userService.deleteUser(id);
        AiPlatformLoggerUtil.info(LogFileEnum.BIZ_SERVICE, "删除用户成功 id={}", id);
    }
}
