package com.jakt.aiplatform.app.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 更新用户信息表请求 DTO。
 */
public record UserUpdateRequest(
        /** 部门ID。 */
        Long deptId,
        /** 登录账号。 */
        @NotBlank(message = "登录账号不能为空")
        @Size(max = 30, message = "登录账号长度不能超过 30")
        String loginName,
        /** 用户昵称。 */
        String userName,
        /** 用户类型（00系统用户 01注册用户）。 */
        String userType,
        /** 用户邮箱。 */
        String email,
        /** 手机号码。 */
        String phonenumber,
        /** 用户性别（0男 1女 2未知）。 */
        String sex,
        /** 头像路径。 */
        String avatar,
        /** 密码。 */
        String password,
        /** 盐加密。 */
        String salt,
        /** 账号状态（0正常 1停用）。 */
        String status,
        /** 最后登录IP。 */
        String loginIp,
        /** 最后登录时间。 */
        LocalDateTime loginDate,
        /** 密码最后更新时间。 */
        LocalDateTime pwdUpdateDate,
        /** 备注。 */
        String remark
) {
}
