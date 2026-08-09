package com.jakt.aiplatform.app.biz;

import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.param.UserQueryParam;
import com.jakt.aiplatform.core.model.result.PageResult;
import com.jakt.aiplatform.core.repository.UserRepository;
import com.jakt.aiplatform.core.service.UserDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 用户业务服务：用例编排。输入输出都是领域模型，不做前端格式转换。
 */
@Service
public class UserBizService {

    private static final Logger log = LoggerFactory.getLogger(UserBizService.class);

    private final UserDomainService userDomainService;

    private final UserRepository userRepository;

    public UserBizService(UserDomainService userDomainService,
                          UserRepository userRepository) {
        this.userDomainService = userDomainService;
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        User created = userDomainService.createUser(user);
        log.info("创建用户成功 id={} username={}", created.getId(), created.getUsername());
        return created;
    }

    public User getUser(Long id) {
        return userDomainService.getUser(id);
    }

    public PageResult<User> pageUsers(UserQueryParam query) {
        return userRepository.findPage(query);
    }

    public User updateUser(User user) {
        User updated = userDomainService.updateUser(user);
        log.info("更新用户成功 id={}", updated.getId());
        return updated;
    }

    public void deleteUser(Long id) {
        userDomainService.deleteUser(id);
        log.info("删除用户成功 id={}", id);
    }
}
