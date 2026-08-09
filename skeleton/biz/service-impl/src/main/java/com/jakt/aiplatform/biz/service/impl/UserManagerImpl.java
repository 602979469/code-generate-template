package com.jakt.aiplatform.biz.service.impl;

import com.jakt.aiplatform.biz.service.UserManager;
import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.param.UserQueryParam;
import com.jakt.aiplatform.core.model.result.PageResult;
import com.jakt.aiplatform.core.service.UserDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户信息表 Manager 实现：用例编排，只依赖 core-model 与 core-service（DomainService），不直接触碰仓储。
 */
@Service
public class UserManagerImpl implements UserManager {

    private static final Logger log = LoggerFactory.getLogger(UserManagerImpl.class);

    /** 用户信息表领域服务。 */
    private final UserDomainService userDomainService;

    public UserManagerImpl(UserDomainService userDomainService) {
        this.userDomainService = userDomainService;
    }

    @Override
    public User createUser(User user) {
        User created = userDomainService.createUser(user);
        log.info("创建用户信息成功 id={}", created.getId());
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
        log.info("更新用户信息成功 id={}", updated.getId());
        return updated;
    }

    @Override
    public void updateByCondition(User user) {
        userDomainService.updateByCondition(user);
        log.info("按条件更新用户信息成功 id={}", user.getId());
    }

    @Override
    public void deleteUser(Long id) {
        userDomainService.deleteUser(id);
        log.info("删除用户信息成功 id={}", id);
    }
}
