package com.jakt.aiplatform.core.repository.convertor;

import com.jakt.aiplatform.common.dal.dataobject.UserDO;
import com.jakt.aiplatform.core.model.domain.User;
import org.springframework.beans.BeanUtils;

/**
 * 用户信息表 DO 与 Model 互转。
 */
public class UserConvertor {

    public static User toModel(UserDO userDO) {
        if (userDO == null) {
            return null;
        }
        User user = new User();
        BeanUtils.copyProperties(userDO, user);
        return user;
    }

    public static UserDO toDO(User user) {
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(user, userDO);
        return userDO;
    }
}
