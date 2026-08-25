package ${basePackage}.common.dal.mapper;

import ${basePackage}.common.dal.dataobject.${className}DO;
import ${basePackage}.common.dal.query.${className}DalQuery;
import org.apache.ibatis.annotations.Mapper;
<#if compositePk>
import org.apache.ibatis.annotations.Param;
</#if>
import java.util.List;

/**
 * ${entityName} Mapper。SQL 全部在 resources/mapper/${className}Mapper.xml 中；
 * 查询参数使用 common-dal 的 ${className}DalQuery，common-dal 不依赖 core-model。
 */
@Mapper
public interface ${className}Mapper {

    /**
     * 按主键查询。
     *
     * @param ${pkCallArgs} 主键
     * @return ${entityName}数据对象
     */
    ${className}DO selectBy${pkMethodName}(${pkMapperArgs});

    /**
     * 分页查询：SQL 含 LIMIT #{offset}, #{pageSize}，配合 countByQuery 组装分页结果。
     *
     * @param query 查询参数
     * @return 当前页数据
     */
    List<${className}DO> selectPage(${className}DalQuery query);

    /**
     * 列表查询：与 {@link #selectPage} 完全一致，仅去掉 LIMIT 一行，返回全量结果。
     *
     * @param query 查询参数
     * @return 全量数据
     */
    List<${className}DO> selectList(${className}DalQuery query);

    /**
     * 单条查询：与 {@link #selectList} 一致但不加 LIMIT；多条由 MyBatis 抛 TooManyResultsException，不做特殊处理。
     *
     * @param query 查询参数
     * @return 至多一条数据，无匹配返回 null
     */
    ${className}DO selectOne(${className}DalQuery query);

    /**
     * 按查询条件统计总条数，用于分页。
     *
     * @param query 查询参数
     * @return 总条数
     */
    long countByQuery(${className}DalQuery query);

    /**
     * 新增，返回受影响行数；自增主键回填到入参 DO。
     *
     * @param ${classNameLower}DO 数据对象
     * @return 受影响行数
     */
    int insert(${className}DO ${classNameLower}DO);

    /**
     * 按主键更新，返回受影响行数。
     *
     * @param ${classNameLower}DO 数据对象
     * @return 受影响行数
     */
    int update(${className}DO ${classNameLower}DO);

    /**
     * 按条件更新：只更新传入的非空字段（部分更新），适合只改几个字段的场景。
     * 注意：无法把字段更新为 null，需要置 null 请用 {@link #update}；
     * <#if updateTimeAuto>create_time/update_time 由数据库自动维护，不参与更新。<#else>update_time 由生成代码用 NOW() 维护。</#if>
     *
     * @param ${classNameLower}DO 数据对象（至少含主键）
     * @return 受影响行数
     */
    int updateByCondition(${className}DO ${classNameLower}DO);

    /**
     * 按主键删除，返回受影响行数。
     *
     * @param ${pkCallArgs} 主键
     * @return 受影响行数
     */
    int deleteBy${pkMethodName}(${pkMapperArgs});
}
