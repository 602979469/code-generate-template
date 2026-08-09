package com.jakt.aiplatform.biz.service;

import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.param.UserQueryParam;
import com.jakt.aiplatform.core.model.result.PageResult;

import java.util.List;

/**
 * 用户管理类接口定义
 * 
 */
public interface UserManager {

    /**
     * 创建用户
     *
     * @param user 用户
     * @return 创建成功后的用户
     */
    User createUser(User user);

    /**
     * 按 ID 查询用户
     *
     * @param id 用户 ID
     * @return 用户
     */
    User getUser(Long id);

    /**
     * 分页查询用户
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<User> pageUsers(UserQueryParam query);

    /**
     * 列表查询用户
     *
     * @param query 查询参数
     * @return 用户列表
     */
    List<User> listUsers(UserQueryParam query);

    /**
     * 更新用户（全量）。
     *
     * @param user 用户（含主键）
     */
    void updateUser(User user);

    /**
     * 按条件更新用户（只更新传入的非空字段）。
     *
     * @param user 用户（至少含主键）
     */
    void updateByCondition(User user);

    /**
     * 删除用户。
     *
     * @param id 用户 ID
     */
    void deleteUser(Long id);
}
