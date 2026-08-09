package com.jakt.aiplatform.app.web.param;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 更新用户信息表请求 DTO。
 *
 * <p>校验规则与 sys_user 表字段对齐：非空 + varchar 长度，不做业务自定义规则。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest extends BaseRequest {
    /** 部门ID。 */
    private Long deptId;
    /** 登录账号。 */
    @NotBlank(message = "登录账号不能为空")
    @Size(max = 30, message = "登录账号长度不能超过 30")
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
}
