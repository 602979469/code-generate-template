package com.jakt.aiplatform.app.biz;

import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.param.UserQueryParam;
import com.jakt.aiplatform.core.model.result.PageResult;
import com.jakt.aiplatform.core.service.UserDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户信息表 Manager：用例编排。输入输出都是领域模型，不做前端格式转换。
 * 只依赖 core-model 与 core-service（DomainService），不直接触碰仓储。
 */
@Service
public class UserManager {

    private static final Logger log = LoggerFactory.getLogger(UserManager.class);

    /** 用户信息表领域服务。 */
    private final UserDomainService userDomainService;

    public UserManager(UserDomainService userDomainService) {
        this.userDomainService = userDomainService;
    }

    /**
     * 创建用户信息。
     *
     * @param user 用户信息
     * @return 创建成功后的用户信息
     */
    public User createUser(User user) {
        User created = userDomainService.createUser(user);
        log.info("创建用户信息成功 id={}", created.getId());
        return created;
    }

    /**
     * 按 ID 查询用户信息。
     *
     * @param id 用户信息 ID
     * @return 用户信息
     */
    public User getUser(Long id) {
        return userDomainService.getUser(id);
    }

    /**
     * 分页查询用户信息。
     *
     * @param query 查询参数
     * @return 分页结果
     */
    public PageResult<User> pageUsers(UserQueryParam query) {
        return userDomainService.findPage(query);
    }

    /**
     * 列表查询用户信息。
     *
     * @param query 查询参数
     * @return 用户信息列表
     */
    public List<User> listUsers(UserQueryParam query) {
        return userDomainService.findList(query);
    }

    /**
     * 更新用户信息（全量）。
     * 注意：PUT 为全量覆盖，未传字段会被置 NULL；部分更新请用 {@link #updateByCondition}。
     *
     * @param user 用户信息（含主键）
     * @return 更新后的用户信息
     */
    public User updateUser(User user) {
        User updated = userDomainService.updateUser(user);
        log.info("更新用户信息成功 id={}", updated.getId());
        return updated;
    }

    /**
     * 按条件更新用户信息（只更新传入的非空字段）。
     *
     * @param user 用户信息（至少含主键）
     */
    public void updateByCondition(User user) {
        userDomainService.updateByCondition(user);
        log.info("按条件更新用户信息成功 id={}", user.getId());
    }

    /**
     * 删除用户信息。
     *
     * @param id 用户信息 ID
     */
    public void deleteUser(Long id) {
        userDomainService.deleteUser(id);
        log.info("删除用户信息成功 id={}", id);
    }
}
