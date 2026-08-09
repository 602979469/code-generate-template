package com.jakt.aiplatform.app.web.assembler;

import com.jakt.aiplatform.app.web.dto.UserCreateRequest;
import com.jakt.aiplatform.app.web.dto.UserQueryRequest;
import com.jakt.aiplatform.app.web.dto.UserResponse;
import com.jakt.aiplatform.app.web.dto.UserUpdateRequest;
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
        user.setDeptId(request.deptId());
        user.setLoginName(request.loginName());
        user.setUserName(request.userName());
        user.setUserType(request.userType());
        user.setEmail(request.email());
        user.setPhonenumber(request.phonenumber());
        user.setSex(request.sex());
        user.setAvatar(request.avatar());
        user.setPassword(request.password());
        user.setSalt(request.salt());
        user.setStatus(request.status());
        user.setLoginIp(request.loginIp());
        user.setLoginDate(request.loginDate());
        user.setPwdUpdateDate(request.pwdUpdateDate());
        user.setRemark(request.remark());
        return user;
    }

    public static User toModel(UserUpdateRequest request, Long id) {
        User user = new User();
        user.setId(id);
        user.setDeptId(request.deptId());
        user.setLoginName(request.loginName());
        user.setUserName(request.userName());
        user.setUserType(request.userType());
        user.setEmail(request.email());
        user.setPhonenumber(request.phonenumber());
        user.setSex(request.sex());
        user.setAvatar(request.avatar());
        user.setPassword(request.password());
        user.setSalt(request.salt());
        user.setStatus(request.status());
        user.setLoginIp(request.loginIp());
        user.setLoginDate(request.loginDate());
        user.setPwdUpdateDate(request.pwdUpdateDate());
        user.setRemark(request.remark());
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
        param.setCreateTime(request.getCreateTime());
        param.setUpdateTime(request.getUpdateTime());
        param.setPageNum(request.getPageNum() == null ? 1 : request.getPageNum());
        param.setPageSize(request.getPageSize() == null ? 10 : request.getPageSize());
        return param;
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getDeptId(),
                user.getLoginName(),
                user.getUserName(),
                user.getUserType(),
                user.getEmail(),
                user.getPhonenumber(),
                user.getSex(),
                user.getAvatar(),
                user.getPassword(),
                user.getSalt(),
                user.getStatus(),
                user.getLoginIp(),
                user.getLoginDate(),
                user.getPwdUpdateDate(),
                user.getRemark(),
                user.getCreateTime(),
                user.getUpdateTime()
        );
    }
}
