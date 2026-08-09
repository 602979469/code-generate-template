package com.jakt.aiplatform.core.repository.convertor;

import com.jakt.aiplatform.common.dal.dataobject.UserDO;
import com.jakt.aiplatform.core.model.domain.User;
import org.springframework.beans.BeanUtils;

/**
 * 用户信息表 DO 与领域模型互转，只存在于 repository。
 */
public final class UserConvertor {

    private UserConvertor() {
    }

    /**
     * DO → 领域模型。
     *
     * @param userDO 用户信息表数据对象；为空返回 null
     * @return 用户信息领域模型
     */
    public static User toModel(UserDO userDO) {
        if (userDO == null) {
            return null;
        }
        User user = new User();
        BeanUtils.copyProperties(userDO, user);
        return user;
    }

    /**
     * 领域模型 → DO。
     *
     * @param user 用户信息领域模型
     * @return 用户信息表数据对象
     */
    public static UserDO toDO(User user) {
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(user, userDO);
        return userDO;
    }
}
