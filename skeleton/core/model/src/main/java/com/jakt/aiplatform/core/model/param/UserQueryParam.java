package com.jakt.aiplatform.core.model.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息表查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserQueryParam extends PageParam {
    /** 主键ID。 */
    private Long id;

    /** 部门ID。 */
    private Long deptId;

    /** 登录账号。 */
    private String loginName;

    /** 用户昵称。 */
    private String userName;

    /** 用户类型（00系统用户 01注册用户）。 */
    private String userType;

    /** 用户邮箱。 */
    private String email;

    /** 手机号码。 */
    private String phonenumber;

    /** 用户性别（0男 1女 2未知）。 */
    private String sex;

    /** 头像路径。 */
    private String avatar;

    /** 密码。 */
    private String password;

    /** 盐加密。 */
    private String salt;

    /** 账号状态（0正常 1停用）。 */
    private String status;

    /** 最后登录IP。 */
    private String loginIp;

    /** 最后登录时间。 */
    private LocalDateTime loginDate;

    /** 密码最后更新时间。 */
    private LocalDateTime pwdUpdateDate;

    /** 备注。 */
    private String remark;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 更新时间。 */
    private LocalDateTime updateTime;

}
