package com.jakt.aiplatform.app.web.controller;

import com.jakt.aiplatform.app.biz.UserBizService;
import com.jakt.aiplatform.app.web.assembler.UserAssembler;
import com.jakt.aiplatform.app.web.param.UserCreateRequest;
import com.jakt.aiplatform.app.web.param.UserQueryRequest;
import com.jakt.aiplatform.app.web.param.UserUpdateRequest;
import com.jakt.aiplatform.app.web.result.UserResponse;
import com.jakt.aiplatform.app.web.result.AiPlatformResult;
import com.jakt.aiplatform.app.web.template.AiPlatformTemplate;
import com.jakt.aiplatform.common.util.tools.AiPlatformParamValidator;
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

/**
 * 用户信息表管理接口。Controller 只做参数校验、DTO 转换与结果包装，不含业务规则；
 * 参数校验、异常封装、请求日志与 Result 组装统一交给 AiPlatformTemplate。
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "用户信息表管理")
public class UserController {

    /** 用户信息表业务服务。 */
    private final UserBizService userBizService;

    public UserController(UserBizService userBizService) {
        this.userBizService = userBizService;
    }

    /**
     * 创建用户信息表。
     *
     * @param request 创建用户信息表请求体
     * @return 创建成功后的用户信息表信息
     */
    @PostMapping
    public AiPlatformResult<UserResponse> create(@RequestBody UserCreateRequest request) {
        return AiPlatformTemplate.execute(request, new AiPlatformTemplate.Callback<UserCreateRequest, UserResponse>() {

            @Override
            public void beforeService(UserCreateRequest param) {
                AiPlatformParamValidator.validate(param);
            }

            @Override
            public UserResponse execute(UserCreateRequest param) {
                User user = userBizService.createUser(UserAssembler.toModel(param));
                return UserAssembler.toResponse(user);
            }

            @Override
            public void afterService(UserCreateRequest param, UserResponse result) {
            }
        });
    }

    /**
     * 按 ID 查询用户信息表。
     *
     * @param id 用户信息表 ID
     * @return 用户信息表信息
     */
    @GetMapping("/{id}")
    public AiPlatformResult<UserResponse> get(@PathVariable Long id) {
        return AiPlatformTemplate.execute(id, new AiPlatformTemplate.Callback<Long, UserResponse>() {

            @Override
            public void beforeService(Long param) {
                AiPlatformParamValidator.validate(param);
            }

            @Override
            public UserResponse execute(Long param) {
                return UserAssembler.toResponse(userBizService.getUser(param));
            }

            @Override
            public void afterService(Long param, UserResponse result) {
            }
        });
    }

    /**
     * 分页查询用户信息表。
     *
     * @param request 查询条件（含分页参数与时间区间）
     * @return 分页结果
     */
    @GetMapping("/page")
    public AiPlatformResult<PageResult<UserResponse>> page(UserQueryRequest request) {
        return AiPlatformTemplate.execute(request, new AiPlatformTemplate.Callback<UserQueryRequest, PageResult<UserResponse>>() {

            @Override
            public void beforeService(UserQueryRequest param) {
                AiPlatformParamValidator.validate(param);
            }

            @Override
            public PageResult<UserResponse> execute(UserQueryRequest param) {
                PageResult<User> page = userBizService.pageUsers(UserAssembler.toQueryParam(param));
                return new PageResult<>(page.getTotal(), param.getPageNum(), param.getPageSize(),
                        page.getDataList().stream().map(UserAssembler::toResponse).toList());
            }

            @Override
            public void afterService(UserQueryRequest param, PageResult<UserResponse> result) {
            }
        });
    }

    /**
     * 更新用户信息表（全量）。
     *
     * @param id      用户信息表 ID
     * @param request 更新内容
     * @return 更新后的用户信息表信息
     */
    @PutMapping("/{id}")
    public AiPlatformResult<UserResponse> update(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        return AiPlatformTemplate.execute(request, new AiPlatformTemplate.Callback<UserUpdateRequest, UserResponse>() {

            @Override
            public void beforeService(UserUpdateRequest param) {
                AiPlatformParamValidator.validate(param);
            }

            @Override
            public UserResponse execute(UserUpdateRequest param) {
                User user = userBizService.updateUser(UserAssembler.toModel(param, id));
                return UserAssembler.toResponse(user);
            }

            @Override
            public void afterService(UserUpdateRequest param, UserResponse result) {
            }
        });
    }

    /**
     * 删除用户信息表。
     *
     * @param id 用户信息表 ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public AiPlatformResult<Void> delete(@PathVariable Long id) {
        return AiPlatformTemplate.executeWithoutResult(id, new AiPlatformTemplate.CallbackWithoutResult<Long>() {

            @Override
            public void beforeService(Long param) {
                AiPlatformParamValidator.validate(param);
            }

            @Override
            public void execute(Long param) {
                userBizService.deleteUser(param);
            }

            @Override
            public void afterService(Long param) {
            }
        });
    }
}
