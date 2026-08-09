package com.jakt.aiplatform.core.repository;

import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.param.UserQueryParam;
import com.jakt.aiplatform.core.model.result.PageResult;

/**
 * 用户信息表仓储：封装 Mapper，对外只暴露领域模型。当前阶段单表操作不引入事务。
 */
public interface UserRepository {

    /**
     * 按主键查询。
     *
     * @param id 主键
     * @return 用户信息表领域模型
     */
    User findById(Long id);

    /**
     * 分页查询。
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<User> findPage(UserQueryParam query);

    /**
     * 新增。
     *
     * @param user 用户信息表
     * @return 新增后的用户信息表（主键已回填）
     */
    User insert(User user);

    /**
     * 更新。
     *
     * @param user 用户信息表
     * @return 更新后的用户信息表
     */
    User update(User user);

    /**
     * 按主键删除。
     *
     * @param id 主键
     */
    void deleteById(Long id);
}
