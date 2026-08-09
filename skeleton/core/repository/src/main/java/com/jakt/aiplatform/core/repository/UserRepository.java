package com.jakt.aiplatform.core.repository;

import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.param.UserQueryParam;
import com.jakt.aiplatform.core.model.result.PageResult;


/**
 * 用户仓储：封装 Mapper，对外只暴露领域模型。当前阶段单表操作不引入事务。
 *
 * <p>组合查询示例：{@link #findPage(UserQueryParam)} 一次调用 count + list 两个 Mapper 方法，
 * 组装成统一的 {@link PageResult} 返回。
 */
public interface UserRepository {

    User findById(Long id);

    User findByUsername(String username);

    PageResult<User> findPage(UserQueryParam query);

    User insert(User user);

    User update(User user);

    void deleteById(Long id);

}
