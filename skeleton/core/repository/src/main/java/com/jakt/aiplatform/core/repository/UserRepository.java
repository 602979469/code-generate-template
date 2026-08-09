package com.jakt.aiplatform.core.repository;

import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.param.UserQueryParam;
import com.jakt.aiplatform.core.model.result.PageResult;

import java.util.List;

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
     * 列表查询。
     *
     * @param query 查询参数
     * @return 用户信息表列表
     */
    List<User> findList(UserQueryParam query);

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
     * 按条件更新：只更新传入的非空字段（部分更新）。
     * 注意：无法把字段更新为 null，需要置 null 请用 {@link #update}；create_time/update_time 由数据库自动维护。
     *
     * @param user 用户信息表（至少含主键）
     */
    void updateByCondition(User user);

    /**
     * 按主键删除。
     *
     * @param id 主键
     */
    void deleteById(Long id);
}
