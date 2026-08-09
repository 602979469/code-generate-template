package com.jakt.aiplatform.core.repository.impl;

import com.jakt.aiplatform.common.dal.dataobject.UserDO;
import com.jakt.aiplatform.common.dal.mapper.UserMapper;
import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.param.UserQueryParam;
import com.jakt.aiplatform.core.model.result.PageResult;
import com.jakt.aiplatform.core.repository.UserRepository;
import com.jakt.aiplatform.core.repository.assembler.UserAssembler;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.jakt.aiplatform.core.repository.assembler.UserAssembler.toDO;
import static com.jakt.aiplatform.core.repository.assembler.UserAssembler.toModel;

/**
 * 用户信息表仓储：封装 Mapper，对外只暴露领域模型。当前阶段单表操作不引入事务。
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    /** 用户信息表 Mapper。 */
    private final UserMapper userMapper;

    public UserRepositoryImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User findById(Long id) {
        return toModel(userMapper.selectById(id));
    }

    @Override
    public List<User> findList(UserQueryParam query) {
        return userMapper.selectList(query).stream().map(UserAssembler::toModel).toList();
    }

    @Override
    public PageResult<User> findPage(UserQueryParam query) {
        List<UserDO> doList = userMapper.selectPage(query);
        long total = userMapper.countByQuery(query);
        List<User> list = doList.stream().map(UserAssembler::toModel).toList();
        return new PageResult<>(total, query.getPageNum(), query.getPageSize(), list);
    }

    @Override
    public User insert(User user) {
        UserDO userDO = toDO(user);
        userMapper.insert(userDO);
        return toModel(userDO);
    }

    @Override
    public User update(User user) {
        UserDO userDO = toDO(user);
        userMapper.update(userDO);
        return toModel(userDO);
    }

    @Override
    public void updateByCondition(User user) {
        userMapper.updateByCondition(toDO(user));
    }

    @Override
    public void deleteById(Long id) {
        userMapper.deleteById(id);
    }
}
