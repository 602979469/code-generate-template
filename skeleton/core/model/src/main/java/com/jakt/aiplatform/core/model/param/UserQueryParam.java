package com.jakt.aiplatform.core.model.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 用户查询参数。仓储层把多个查询条件塞进这个对象，再调用 Mapper 通用查询方法。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserQueryParam extends PageParam {

    /** 用户名（模糊匹配）。 */
    private String username;

    /** 昵称（模糊匹配）。 */
    private String nickname;

    /** 状态。 */
    private Integer status;
}
