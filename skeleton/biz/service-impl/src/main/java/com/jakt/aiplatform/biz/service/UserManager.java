package com.jakt.aiplatform.biz.service;

import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.param.UserQueryParam;
import com.jakt.aiplatform.core.model.result.PageResult;

import java.util.List;

/**
 * 用户信息表 Manager：用例编排，输入输出都是领域模型，不做前端格式转换。
 * 实现类为 UserManagerImpl（biz.service.impl 包），web 层依赖本接口。
 */
public interface UserManager {

    /**
     * 创建用户信息。
     *
     * @param user 用户信息
     * @return 创建成功后的用户信息
     */
    User createUser(User user);

    /**
     * 按 ID 查询用户信息。
     *
     * @param id 用户信息 ID
     * @return 用户信息
     */
    User getUser(Long id);

    /**
     * 分页查询用户信息。
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<User> pageUsers(UserQueryParam query);

    /**
     * 列表查询用户信息。
     * 预留能力：web 未接线，业务方按需暴露。
     *
     * @param query 查询参数
     * @return 用户信息列表
     */
    List<User> listUsers(UserQueryParam query);

    /**
     * 更新用户信息（全量）。
     * 注意：PUT 为全量覆盖，未传字段会被置 NULL；部分更新请用 {@link #updateByCondition}。
     *
     * @param user 用户信息（含主键）
     * @return 更新后的用户信息
     */
    User updateUser(User user);

    /**
     * 按条件更新用户信息（只更新传入的非空字段）。
     * 预留能力：web 未接线，业务方按需暴露。
     *
     * @param user 用户信息（至少含主键）
     */
    void updateByCondition(User user);

    /**
     * 删除用户信息。
     *
     * @param id 用户信息 ID
     */
    void deleteUser(Long id);
}
