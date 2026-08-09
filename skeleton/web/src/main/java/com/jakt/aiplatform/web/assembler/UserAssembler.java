package com.jakt.aiplatform.web.assembler;

import cn.hutool.core.util.ObjectUtil;
import com.jakt.aiplatform.web.param.UserCreateRequest;
import com.jakt.aiplatform.web.param.UserQueryRequest;
import com.jakt.aiplatform.web.param.UserUpdateRequest;
import com.jakt.aiplatform.web.result.UserResponse;
import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.param.UserQueryParam;

/**
 * 用户信息表对象组装器：DTO 与领域模型互转，只存在于 web。
 */
public final class UserAssembler {

    private UserAssembler() {
    }

    /**
     * 创建请求 DTO → 领域模型。
     *
     * @param request 创建用户信息请求 DTO
     * @return 用户信息领域模型
     */
    public static User toModel(UserCreateRequest request) {
        User user = new User();
        user.setDeptId(request.getDeptId());
        user.setLoginName(request.getLoginName());
        user.setUserName(request.getUserName());
        user.setUserType(request.getUserType());
        user.setEmail(request.getEmail());
        user.setPhonenumber(request.getPhonenumber());
        user.setSex(request.getSex());
        user.setAvatar(request.getAvatar());
        user.setPassword(request.getPassword());
        user.setSalt(request.getSalt());
        user.setStatus(request.getStatus());
        user.setLoginIp(request.getLoginIp());
        user.setLoginDate(request.getLoginDate());
        user.setPwdUpdateDate(request.getPwdUpdateDate());
        user.setRemark(request.getRemark());
        return user;
    }

    /**
     * 更新请求 DTO + 路径 ID → 领域模型。
     *
     * @param request 更新用户信息请求 DTO
     * @param id      路径中的用户信息 ID
     * @return 用户信息领域模型
     */
    public static User toModel(UserUpdateRequest request, Long id) {
        User user = new User();
        user.setId(id);
        user.setDeptId(request.getDeptId());
        user.setLoginName(request.getLoginName());
        user.setUserName(request.getUserName());
        user.setUserType(request.getUserType());
        user.setEmail(request.getEmail());
        user.setPhonenumber(request.getPhonenumber());
        user.setSex(request.getSex());
        user.setAvatar(request.getAvatar());
        user.setPassword(request.getPassword());
        user.setSalt(request.getSalt());
        user.setStatus(request.getStatus());
        user.setLoginIp(request.getLoginIp());
        user.setLoginDate(request.getLoginDate());
        user.setPwdUpdateDate(request.getPwdUpdateDate());
        user.setRemark(request.getRemark());
        return user;
    }

    /**
     * 查询请求 DTO → 查询参数。
     *
     * @param request 用户信息查询请求 DTO
     * @return 用户信息查询参数
     */
    public static UserQueryParam toQueryParam(UserQueryRequest request) {
        UserQueryParam param = new UserQueryParam();
        param.setId(request.getId());
        param.setDeptId(request.getDeptId());
        param.setLoginName(request.getLoginName());
        param.setUserName(request.getUserName());
        param.setUserType(request.getUserType());
        param.setEmail(request.getEmail());
        param.setPhonenumber(request.getPhonenumber());
        param.setSex(request.getSex());
        param.setAvatar(request.getAvatar());
        param.setPassword(request.getPassword());
        param.setSalt(request.getSalt());
        param.setStatus(request.getStatus());
        param.setLoginIp(request.getLoginIp());
        param.setLoginDate(request.getLoginDate());
        param.setPwdUpdateDate(request.getPwdUpdateDate());
        param.setRemark(request.getRemark());
        param.setCreateTimeBegin(request.getCreateTimeBegin());
        param.setCreateTimeEnd(request.getCreateTimeEnd());
        param.setUpdateTimeBegin(request.getUpdateTimeBegin());
        param.setUpdateTimeEnd(request.getUpdateTimeEnd());
        param.setPageNum(ObjectUtil.defaultIfNull(request.getPageNum(), 1));
        param.setPageSize(ObjectUtil.defaultIfNull(request.getPageSize(), 10));
        return param;
    }

    /**
     * 领域模型 → 响应 DTO。
     *
     * @param user 用户信息领域模型
     * @return 用户信息响应 DTO
     */
    public static UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setDeptId(user.getDeptId());
        response.setLoginName(user.getLoginName());
        response.setUserName(user.getUserName());
        response.setUserType(user.getUserType());
        response.setEmail(user.getEmail());
        response.setPhonenumber(user.getPhonenumber());
        response.setSex(user.getSex());
        response.setAvatar(user.getAvatar());
        response.setPassword(user.getPassword());
        response.setSalt(user.getSalt());
        response.setStatus(user.getStatus());
        response.setLoginIp(user.getLoginIp());
        response.setLoginDate(user.getLoginDate());
        response.setPwdUpdateDate(user.getPwdUpdateDate());
        response.setRemark(user.getRemark());
        response.setCreateTime(user.getCreateTime());
        response.setUpdateTime(user.getUpdateTime());
        return response;
    }
}
