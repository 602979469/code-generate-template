package com.jakt.aiplatform.core.model.domain;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 系统用户领域模型。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseModel {

    /** 用户名，唯一。 */
    private String username;

    /** 昵称。 */
    private String nickname;

    /** 邮箱。 */
    private String email;

    /** 手机号。 */
    private String phone;

    /** 状态：0 正常，1 停用。 */
    private Integer status;

}
