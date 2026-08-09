package com.jakt.aiplatform.common.dal.mapper;

import com.jakt.aiplatform.common.dal.dataobject.UserDO;
import com.jakt.aiplatform.core.model.param.UserQueryParam;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户表 Mapper。SQL 全部在 resources/mapper/UserMapper.xml 中。
 */
@Mapper
public interface UserMapper {

    /** 按主键查询用户。 */
    UserDO selectById(Long id);

    /** 按登录账号精确查询用户。 */
    UserDO selectByLoginName(String loginName);

    /** 分页查询用户列表：SQL 含 LIMIT #{offset}, #{pageSize}，配合 countByQuery 组装分页结果。 */
    List<UserDO> selectPage(UserQueryParam query);

    /** 查询用户列表：与 {@link #selectPage} 完全一致，仅去掉 LIMIT 一行，返回全量结果。 */
    List<UserDO> selectList(UserQueryParam query);

    /** 按查询条件统计总条数，用于分页。 */
    long countByQuery(UserQueryParam query);

    /** 新增用户，返回受影响行数；自增主键回填到 {@code userDO.id}。 */
    int insert(UserDO userDO);

    /** 按主键更新用户，返回受影响行数。 */
    int update(UserDO userDO);

    /** 按主键删除用户，返回受影响行数。 */
    int deleteById(Long id);
}
