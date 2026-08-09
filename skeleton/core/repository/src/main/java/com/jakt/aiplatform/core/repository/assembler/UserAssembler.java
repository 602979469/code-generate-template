package com.jakt.aiplatform.core.repository.assembler;

import com.jakt.aiplatform.common.dal.dataobject.UserDO;
import com.jakt.aiplatform.core.model.domain.User;
import org.springframework.beans.BeanUtils;

/**
 * 用户信息表 DO 与领域模型互转，只存在于 repository。
 */
public final class UserAssembler {

    private UserAssembler() {
    }

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
