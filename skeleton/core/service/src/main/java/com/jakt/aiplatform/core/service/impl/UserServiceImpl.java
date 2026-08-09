package com.jakt.aiplatform.core.service.impl;

import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.param.UserQueryParam;
import com.jakt.aiplatform.core.model.result.PageResult;
import com.jakt.aiplatform.core.repository.UserRepository;
import com.jakt.aiplatform.core.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户领域服务实现：承载用户相关的业务规则。只写规则，不碰持久化细节。
 */
@Service
public class UserServiceImpl implements UserService {

    /** 用户仓储。 */
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(User user) {
        return userRepository.insert(user);
    }

    @Override
    public void updateUser(User user) {
        userRepository.update(user);
    }

    @Override
    public void updateByCondition(User user) {
        userRepository.updateByCondition(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User getUser(Long id) {
        return userRepository.findById(id);
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
