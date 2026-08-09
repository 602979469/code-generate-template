package com.jakt.aiplatform.app.web.dto;

import java.time.LocalDateTime;

/**
 * 用户信息表响应 DTO。
 */
public record UserResponse(
        /** 主键 ID。 */
        Long id,
        /** 部门ID。 */
        Long deptId,
        /** 登录账号。 */
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
        String remark,
        /** 创建时间。 */
        LocalDateTime createTime,
        /** 更新时间。 */
        LocalDateTime updateTime
) {
}
