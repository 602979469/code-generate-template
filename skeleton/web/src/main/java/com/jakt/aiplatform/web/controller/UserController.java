package com.jakt.aiplatform.web.controller;

import cn.hutool.core.util.ObjectUtil;
import com.jakt.aiplatform.biz.service.UserManager;
import com.jakt.aiplatform.web.assembler.UserAssembler;
import com.jakt.aiplatform.web.checker.UserParamChecker;
import com.jakt.aiplatform.web.param.UserCreateRequest;
import com.jakt.aiplatform.web.param.UserQueryRequest;
import com.jakt.aiplatform.web.param.UserUpdateRequest;
import com.jakt.aiplatform.web.result.UserResponse;
import com.jakt.aiplatform.web.result.AiPlatformResult;
import com.jakt.aiplatform.web.template.AiPlatformTemplate;
import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.result.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.ParameterMetaData;

/**
 * 用户管理接口。Controller 只做参数校验、DTO 转换与结果包装，不含业务规则；
 * 参数校验、异常封装、请求日志与 Result 组装统一交给 AiPlatformTemplate。
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "用户管理")
public class UserController {

    /** 用户 Manager。 */
    private final UserManager userManager;

    public UserController(UserManager userManager) {
        this.userManager = userManager;
    }

    /**
     * 创建用户。
     *
     * @param request 创建用户请求体
     * @return 创建成功后的用户信息
     */
    @PostMapping
    public AiPlatformResult<UserResponse> create(@RequestBody UserCreateRequest request) {
        return AiPlatformTemplate.execute(request, new AiPlatformTemplate.Callback<>() {

            @Override
            public void beforeService(UserCreateRequest param) {
                UserParamChecker.checkUserCreateRequest(param);
            }

            @Override
            public UserResponse execute(UserCreateRequest param) {
                User user = userManager.createUser(UserAssembler.toModel(param));
                return UserAssembler.toResponse(user);
            }

            @Override
            public void afterService(UserCreateRequest param, UserResponse result) {
            }
        });
    }

    /**
     * 按 ID 查询用户。
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public AiPlatformResult<UserResponse> get(@PathVariable Long id) {
        return AiPlatformTemplate.execute(id, new AiPlatformTemplate.Callback<>() {

            @Override
            public void beforeService(Long param) {
                UserParamChecker.checkId(param);
            }

            @Override
            public UserResponse execute(Long param) {
                return UserAssembler.toResponse(userManager.getUser(param));
            }

            @Override
            public void afterService(Long param, UserResponse result) {
            }
        });
    }

    /**
     * 分页查询用户。
     *
     * @param request 查询条件（含分页参数与时间区间）
     * @return 分页结果
     */
    @GetMapping("/page")
    public AiPlatformResult<PageResult<UserResponse>> page(UserQueryRequest request) {
        
        return AiPlatformTemplate.execute(request, new AiPlatformTemplate.Callback<>() {

            @Override
            public void beforeService(UserQueryRequest param) {
                UserParamChecker.checkUserQueryRequest(param);
            }

            @Override
            public PageResult<UserResponse> execute(UserQueryRequest param) {
                param = ObjectUtil.defaultIfNull(param, new UserQueryRequest());
                PageResult<User> page = userManager.pageUsers(UserAssembler.toQueryParam(param));
                return new PageResult<>(page.getTotal(), param.getPageNum(), param.getPageSize(),
                        page.getDataList().stream().map(UserAssembler::toResponse).toList());
            }
        });
    }

    /**
     * 更新用户（全量）。
     * 注意：PUT 为全量覆盖，未传字段会被置 NULL；部分更新请走 updateByCondition（Manager/DomainService）。
     *
     * @param id      用户 ID
     * @param request 更新内容
     * @return 更新后的用户信息
     */
    @PutMapping("/{id}")
    public AiPlatformResult<Void> update(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        return AiPlatformTemplate.executeWithoutResult(request, new AiPlatformTemplate.CallbackWithoutResult<>() {

            @Override
            public void beforeService(UserUpdateRequest param) {
                UserParamChecker.checkId(id);
                UserParamChecker.checkUserUpdateRequest(param);
            }

            @Override
            public void execute(UserUpdateRequest param) {
                userManager.updateUser(UserAssembler.toModel(param, id));
            }
        });
    }

    /**
     * 删除用户。
     *
     * @param id 用户 ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public AiPlatformResult<Void> delete(@PathVariable Long id) {
        return AiPlatformTemplate.executeWithoutResult(id, new AiPlatformTemplate.CallbackWithoutResult<>() {

            @Override
            public void beforeService(Long id) {
                UserParamChecker.checkId(id);
            }

            @Override
            public void execute(Long id) {
                userManager.deleteUser(id);
            }
        });
    }
}
