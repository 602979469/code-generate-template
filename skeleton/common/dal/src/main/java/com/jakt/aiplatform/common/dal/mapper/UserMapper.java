package com.jakt.aiplatform.common.dal.mapper;

import com.jakt.aiplatform.common.dal.dataobject.UserDO;
import com.jakt.aiplatform.core.model.param.UserQueryParam;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户信息表 Mapper。SQL 全部在 resources/mapper/UserMapper.xml 中。
 */
@Mapper
public interface UserMapper {

    /**
     * 按主键查询。
     *
     * @param id 主键
     * @return 用户信息表数据对象
     */
    UserDO selectById(Long id);

    /**
     * 分页查询：SQL 含 LIMIT #{offset}, #{pageSize}，配合 countByQuery 组装分页结果。
     *
     * @param query 查询参数
     * @return 当前页数据
     */
    List<UserDO> selectPage(UserQueryParam query);

    /**
     * 列表查询：与 {@link #selectPage} 完全一致，仅去掉 LIMIT 一行，返回全量结果。
     *
     * @param query 查询参数
     * @return 全量数据
     */
    List<UserDO> selectList(UserQueryParam query);

    /**
     * 按查询条件统计总条数，用于分页。
     *
     * @param query 查询参数
     * @return 总条数
     */
    long countByQuery(UserQueryParam query);

    /**
     * 新增，返回受影响行数；自增主键回填到 {@code userDO.id}。
     *
     * @param userDO 数据对象
     * @return 受影响行数
     */
    int insert(UserDO userDO);

    /**
     * 按主键更新，返回受影响行数。
     *
     * @param userDO 数据对象
     * @return 受影响行数
     */
    int update(UserDO userDO);

    /**
     * 按主键删除，返回受影响行数。
     *
     * @param id 主键
     * @return 受影响行数
     */
    int deleteById(Long id);
}
