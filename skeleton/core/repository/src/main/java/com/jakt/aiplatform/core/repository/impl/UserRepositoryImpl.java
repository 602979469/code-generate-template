package com.jakt.aiplatform.core.repository.impl;

import com.jakt.aiplatform.common.dal.dataobject.UserDO;
import com.jakt.aiplatform.common.dal.mapper.UserMapper;
import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.param.UserQueryParam;
import com.jakt.aiplatform.core.model.result.PageResult;
import com.jakt.aiplatform.core.repository.UserRepository;
import com.jakt.aiplatform.core.repository.convertor.UserConvertor;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;
import static com.jakt.aiplatform.core.repository.convertor.UserConvertor.toDO;
import static com.jakt.aiplatform.core.repository.convertor.UserConvertor.toModel;
import java.util.List;

/**
 * 用户仓储：封装 Mapper，对外只暴露领域模型。当前阶段单表操作不引入事务。
 *
 * <p>组合查询示例：{@link #findPage(UserQueryParam)} 一次调用 count + list 两个 Mapper 方法，
 * 组装成统一的 {@link PageResult} 返回。
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    @Resource
    private UserMapper userMapper;

    public User findById(Long id) {
        return toModel(userMapper.selectById(id));
    }

    public User findByUsername(String username) {
        return toModel(userMapper.selectByLoginName(username));
    }

    /** 组合查询：count + list 两个 Mapper 调用，封装为一个分页结果。 */
    public PageResult<User> findPage(UserQueryParam query) {
        List<UserDO> userDOs = userMapper.selectPage(query);
        long total = userMapper.countByQuery(query);
        List<User> users = userDOs.stream().map(UserConvertor::toModel).toList();
        return new PageResult<>(total, query.getPageNum(), query.getPageSize(), users);
    }

    public User insert(User user) {
        UserDO userDO = toDO(user);
        userMapper.insert(userDO);
        return toModel(userDO);
    }

    public User update(User user) {
        UserDO userDO = toDO(user);
        userMapper.update(userDO);
        return toModel(userDO);
    }

    public void deleteById(Long id) {
        userMapper.deleteById(id);
    }

    /** DO 转 Model。 */

}
