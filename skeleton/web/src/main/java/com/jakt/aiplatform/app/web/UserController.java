package com.jakt.aiplatform.app.web;

import com.jakt.aiplatform.app.biz.UserBizService;
import com.jakt.aiplatform.app.web.assembler.UserAssembler;
import com.jakt.aiplatform.app.web.dto.UserCreateRequest;
import com.jakt.aiplatform.app.web.dto.UserQueryRequest;
import com.jakt.aiplatform.app.web.dto.UserResponse;
import com.jakt.aiplatform.app.web.dto.UserUpdateRequest;
import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.result.PageResult;
import com.jakt.aiplatform.core.model.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息表管理接口。Controller 只做参数校验、DTO 转换与结果包装，不含业务规则。
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

    @PostMapping
    public Result<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        User user = userBizService.createUser(UserAssembler.toModel(request));
        Result<UserResponse> result = new Result<>();
        result.setSuccess(true);
        result.setData(UserAssembler.toResponse(user));
        return result;
    }

    @GetMapping("/{id}")
    public Result<UserResponse> get(@PathVariable Long id) {
        Result<UserResponse> result = new Result<>();
        result.setSuccess(true);
        result.setData(UserAssembler.toResponse(userBizService.getUser(id)));
        return result;
    }

    @GetMapping("/page")
    public Result<PageResult<UserResponse>> page(@Valid UserQueryRequest request) {
        PageResult<User> page = userBizService.pageUsers(UserAssembler.toQueryParam(request));
        PageResult<UserResponse> pageResult = new PageResult<>(page.getTotal(), request.getPageNum(), request.getPageSize(),
                page.getDataList().stream().map(UserAssembler::toResponse).toList());
        Result<PageResult<UserResponse>> result = new Result<>();
        result.setSuccess(true);
        result.setData(pageResult);
        return result;
    }

    @PutMapping("/{id}")
    public Result<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        User user = userBizService.updateUser(UserAssembler.toModel(request, id));
        Result<UserResponse> result = new Result<>();
        result.setSuccess(true);
        result.setData(UserAssembler.toResponse(user));
        return result;
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userBizService.deleteUser(id);
        Result<Void> result = new Result<>();
        result.setSuccess(true);
        return result;
    }
}
