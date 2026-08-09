package com.jakt.aiplatform.common.dal.dataobject;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统用户数据对象
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDO extends BaseDO {

    /** 部门ID。 */
    private Long deptId;

    /** 登录账号。 */
    private String loginName;

    /** 用户昵称。 */
    private String userName;

    /** 用户类型：00 系统用户，01 注册用户。 */
    private String userType;

    /** 邮箱。 */
    private String email;

    /** 手机号码。 */
    private String phonenumber;

    /** 性别：0 男，1 女，2 未知。 */
    private String sex;

    /** 头像路径。 */
    private String avatar;

    /** 密码（密文）。 */
    private String password;

    /** 盐加密。 */
    private String salt;

    /** 状态：0 正常，1 停用。 */
    private String status;

    /** 最后登录IP。 */
    private String loginIp;

    /** 最后登录时间。 */
    private LocalDateTime loginDate;

    /** 密码最后更新时间。 */
    private LocalDateTime pwdUpdateDate;

    /** 备注。 */
    private String remark;
}
