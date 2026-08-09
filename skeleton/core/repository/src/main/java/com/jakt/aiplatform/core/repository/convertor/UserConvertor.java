package com.jakt.aiplatform.core.repository.convertor;

import com.jakt.aiplatform.common.dal.dataobject.UserDO;
import com.jakt.aiplatform.core.model.domain.User;

/**
 * 用户信息表 DO 与领域模型互转，只存在于 repository。
 * 显式 get/set 赋值：DO 与 Model 字段类型允许不同（如 status 字符串转枚举），业务方按需调整。
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
        user.setId(userDO.getId());
        user.setDeptId(userDO.getDeptId());
        user.setLoginName(userDO.getLoginName());
        user.setUserName(userDO.getUserName());
        user.setUserType(userDO.getUserType());
        user.setEmail(userDO.getEmail());
        user.setPhonenumber(userDO.getPhonenumber());
        user.setSex(userDO.getSex());
        user.setAvatar(userDO.getAvatar());
        user.setPassword(userDO.getPassword());
        user.setSalt(userDO.getSalt());
        user.setStatus(userDO.getStatus());
        user.setLoginIp(userDO.getLoginIp());
        user.setLoginDate(userDO.getLoginDate());
        user.setPwdUpdateDate(userDO.getPwdUpdateDate());
        user.setRemark(userDO.getRemark());
        user.setCreateTime(userDO.getCreateTime());
        user.setUpdateTime(userDO.getUpdateTime());
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
        userDO.setId(user.getId());
        userDO.setDeptId(user.getDeptId());
        userDO.setLoginName(user.getLoginName());
        userDO.setUserName(user.getUserName());
        userDO.setUserType(user.getUserType());
        userDO.setEmail(user.getEmail());
        userDO.setPhonenumber(user.getPhonenumber());
        userDO.setSex(user.getSex());
        userDO.setAvatar(user.getAvatar());
        userDO.setPassword(user.getPassword());
        userDO.setSalt(user.getSalt());
        userDO.setStatus(user.getStatus());
        userDO.setLoginIp(user.getLoginIp());
        userDO.setLoginDate(user.getLoginDate());
        userDO.setPwdUpdateDate(user.getPwdUpdateDate());
        userDO.setRemark(user.getRemark());
        userDO.setCreateTime(user.getCreateTime());
        userDO.setUpdateTime(user.getUpdateTime());
        return userDO;
    }
}
