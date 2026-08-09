package com.jakt.aiplatform.core.repository.impl;

import com.jakt.aiplatform.common.dal.dataobject.UserDO;
import com.jakt.aiplatform.common.dal.mapper.UserMapper;
import com.jakt.aiplatform.common.util.tools.AiPlatformInvoker;
import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.enums.ErrorCodeEnum;
import com.jakt.aiplatform.core.model.enums.LogFileEnum;
import com.jakt.aiplatform.core.model.param.UserQueryParam;
import com.jakt.aiplatform.core.model.result.PageResult;
import com.jakt.aiplatform.core.model.util.AiPlatformLoggerUtil;
import com.jakt.aiplatform.core.repository.UserRepository;
import com.jakt.aiplatform.core.repository.convertor.UserConvertor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户仓储：封装 Mapper，对外只暴露领域模型。当前阶段单表操作不引入事务。
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    /** 用户 Mapper。 */
    private final UserMapper userMapper;

    public UserRepositoryImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User findById(Long id) {
        return UserConvertor.toModel(userMapper.selectById(id));
    }

    @Override
    public List<User> findList(UserQueryParam query) {
        return userMapper.selectList(query).stream().map(UserConvertor::toModel).toList();
    }

    @Override
    public PageResult<User> findPage(UserQueryParam query) {
        List<UserDO> doList = userMapper.selectPage(query);
        long total = userMapper.countByQuery(query);
        List<User> list = doList.stream().map(UserConvertor::toModel).toList();
        return new PageResult<>(total, query.getPageNum(), query.getPageSize(), list);
    }

    @Override
    public User insert(User user) {
        UserDO userDO = UserConvertor.toDO(user);
        userMapper.insert(userDO);
        return UserConvertor.toModel(userDO);
    }

    @Override
    public void update(User user) {
        UserDO userDO = UserConvertor.toDO(user);
        int affected = userMapper.update(userDO);
        AiPlatformLoggerUtil.info(LogFileEnum.BIZ_SERVICE, "UserRepository.update id={} 影响行数={}", user.getId(), affected);
        AiPlatformInvoker.throwErrWhenTrue(affected == 0, ErrorCodeEnum.UPDATE_FAILED, "更新失败：记录不存在或已被修改");
    }

    @Override
    public void updateByCondition(User user) {
        int affected = userMapper.updateByCondition(UserConvertor.toDO(user));
        AiPlatformLoggerUtil.info(LogFileEnum.BIZ_SERVICE, "UserRepository.updateByCondition id={} 影响行数={}", user.getId(), affected);
        AiPlatformInvoker.throwErrWhenTrue(affected == 0, ErrorCodeEnum.UPDATE_FAILED, "更新失败：记录不存在或已被修改");
    }

    @Override
    public void deleteById(Long id) {
        int affected = userMapper.deleteById(id);
        AiPlatformLoggerUtil.info(LogFileEnum.BIZ_SERVICE, "UserRepository.deleteById id={} 影响行数={}", id, affected);
        AiPlatformInvoker.throwErrWhenTrue(affected == 0, ErrorCodeEnum.DELETE_FAILED, "删除失败：记录不存在或已被删除");
    }
}
