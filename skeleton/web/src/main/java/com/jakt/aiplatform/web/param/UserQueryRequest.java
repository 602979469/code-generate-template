package com.jakt.aiplatform.web.param;

import java.time.LocalDateTime;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 用户信息表查询请求。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserQueryRequest extends BaseRequest {
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

    /** 创建时间起。 */
    private LocalDateTime createTimeBegin;

    /** 创建时间止。 */
    private LocalDateTime createTimeEnd;

    /** 更新时间起。 */
    private LocalDateTime updateTimeBegin;

    /** 更新时间止。 */
    private LocalDateTime updateTimeEnd;

    /** 页码，从 1 开始。 */
    @Min(value = 1, message = "页码不能小于 1")
    private Integer pageNum = 1;

    /** 每页条数。 */
    @Min(value = 1, message = "每页条数不能小于 1")
    @Max(value = 100, message = "每页条数不能超过 100")
    private Integer pageSize = 10;
}
