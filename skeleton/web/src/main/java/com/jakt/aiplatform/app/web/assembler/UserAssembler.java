package com.jakt.aiplatform.app.web.assembler;

import com.jakt.aiplatform.app.web.param.UserCreateRequest;
import com.jakt.aiplatform.app.web.param.UserQueryRequest;
import com.jakt.aiplatform.app.web.param.UserUpdateRequest;
import com.jakt.aiplatform.app.web.result.UserResponse;
import com.jakt.aiplatform.core.model.domain.User;
import com.jakt.aiplatform.core.model.param.UserQueryParam;

/**
 * 用户信息表对象组装器：DTO 与领域模型互转，只存在于 web。
 */
public final class UserAssembler {

    private UserAssembler() {
    }

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
        param.setPageNum(request.getPageNum() == null ? 1 : request.getPageNum());
        param.setPageSize(request.getPageSize() == null ? 10 : request.getPageSize());
        return param;
    }

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
