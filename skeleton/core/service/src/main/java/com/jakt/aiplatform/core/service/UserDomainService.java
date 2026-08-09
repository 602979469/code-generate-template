package com.jakt.aiplatform.core.service;

import com.jakt.aiplatform.common.util.tools.AiPlatformInvoker;
import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.enums.ErrorCodeEnum;
import com.jakt.aiplatform.core.model.param.UserQueryParam;
import com.jakt.aiplatform.core.model.result.PageResult;
import com.jakt.aiplatform.core.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户信息表领域服务：承载用户信息表相关的业务规则。只写规则，不碰持久化细节。
 */
@Service
public class UserDomainService {

    /** 用户信息表仓储。 */
    private final UserRepository userRepository;

    public UserDomainService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 创建用户信息：必填字段校验后入库。
     * createTime/updateTime 由数据库自动维护，领域层不赋值。
     *
     * @param user 用户信息
     * @return 创建后的用户信息（主键已回填）
     */
    public User createUser(User user) {
        AiPlatformInvoker.throwErrWhenBlank(user.getLoginName(), ErrorCodeEnum.PARAM_INVALID, "登录账号不能为空");
        return userRepository.insert(user);
    }

    /**
     * 更新用户信息（全量）：存在性校验后更新。
     * updateTime 由数据库 ON UPDATE CURRENT_TIMESTAMP 自动维护。
     *
     * @param user 用户信息（含主键）
     * @return 更新后的用户信息
     */
    public User updateUser(User user) {
        AiPlatformInvoker.throwErrWhenNull(userRepository.findById(user.getId()), ErrorCodeEnum.RESOURCE_NOT_FOUND);
        return userRepository.update(user);
    }

    /**
     * 按条件更新用户信息（只更新传入的非空字段）。
     *
     * @param user 用户信息（至少含主键）
     */
    public void updateByCondition(User user) {
        AiPlatformInvoker.throwErrWhenNull(userRepository.findById(user.getId()), ErrorCodeEnum.RESOURCE_NOT_FOUND);
        userRepository.updateByCondition(user);
    }

    /**
     * 删除用户信息：存在性校验后删除。
     *
     * @param id 用户信息 ID
     */
    public void deleteUser(Long id) {
        AiPlatformInvoker.throwErrWhenNull(userRepository.findById(id), ErrorCodeEnum.RESOURCE_NOT_FOUND);
        userRepository.deleteById(id);
    }

    /**
     * 按 ID 获取用户信息：不存在时抛业务异常。
     *
     * @param id 用户信息 ID
     * @return 用户信息
     */
    public User getUser(Long id) {
        User user = userRepository.findById(id);
        AiPlatformInvoker.throwErrWhenNull(user, ErrorCodeEnum.RESOURCE_NOT_FOUND);
        return user;
    }

    /**
     * 分页查询用户信息：纯查询，无规则。
     *
     * @param query 查询参数
     * @return 分页结果
     */
    public PageResult<User> findPage(UserQueryParam query) {
        return userRepository.findPage(query);
    }

    /**
     * 列表查询用户信息：纯查询，无规则。
     *
     * @param query 查询参数
     * @return 用户信息列表
     */
    public List<User> findList(UserQueryParam query) {
        return userRepository.findList(query);
    }
}
