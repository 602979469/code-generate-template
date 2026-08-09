package com.jakt.aiplatform.core.service;

import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.param.UserQueryParam;
import com.jakt.aiplatform.core.model.result.PageResult;

import java.util.List;

/**
 * 用户信息表领域服务：承载用户信息表相关的业务规则。只写规则，不碰持久化细节。
 * 实现类为 UserDomainServiceImpl（core.service.impl 包）。
 */
public interface UserDomainService {

    /**
     * 创建用户信息：必填字段校验后入库。
     * createTime/updateTime 由数据库自动维护，领域层不赋值。
     *
     * @param user 用户信息
     * @return 创建后的用户信息（主键已回填）
     */
    User createUser(User user);

    /**
     * 更新用户信息（全量）：存在性校验后更新。
     *
     * @param user 用户信息（含主键）
     * @return 更新后的用户信息
     */
    User updateUser(User user);

    /**
     * 按条件更新用户信息（只更新传入的非空字段）。
     * 全部业务字段均为空时跳过更新（不执行 SQL）。
     *
     * @param user 用户信息（至少含主键）
     */
    void updateByCondition(User user);

    /**
     * 删除用户信息：存在性校验后删除。
     *
     * @param id 用户信息 ID
     */
    void deleteUser(Long id);

    /**
     * 按 ID 获取用户信息：不存在时抛业务异常。
     *
     * @param id 用户信息 ID
     * @return 用户信息
     */
    User getUser(Long id);

    /**
     * 分页查询用户信息：纯查询，无规则。
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<User> findPage(UserQueryParam query);

    /**
     * 列表查询用户信息：纯查询，无规则。
     *
     * @param query 查询参数
     * @return 用户信息列表
     */
    List<User> findList(UserQueryParam query);
}
