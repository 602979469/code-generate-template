package ${basePackage}.common.dal.mapper;

import ${basePackage}.common.dal.dataobject.${className}DO;
import ${basePackage}.core.model.param.${className}QueryParam;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ${tableComment} Mapper。SQL 全部在 resources/mapper/${className}Mapper.xml 中。
 */
@Mapper
public interface ${className}Mapper {

    /**
     * 按主键查询。
     *
     * @param id 主键
     * @return ${tableComment}数据对象
     */
    ${className}DO selectById(Long id);

    /**
     * 分页查询：SQL 含 LIMIT #{offset}, #{pageSize}，配合 countByQuery 组装分页结果。
     *
     * @param query 查询参数
     * @return 当前页数据
     */
    List<${className}DO> selectPage(${className}QueryParam query);

    /**
     * 列表查询：与 {@link #selectPage} 完全一致，仅去掉 LIMIT 一行，返回全量结果。
     *
     * @param query 查询参数
     * @return 全量数据
     */
    List<${className}DO> selectList(${className}QueryParam query);

    /**
     * 按查询条件统计总条数，用于分页。
     *
     * @param query 查询参数
     * @return 总条数
     */
    long countByQuery(${className}QueryParam query);

    /**
     * 新增，返回受影响行数；自增主键回填到 {@code ${classNameLower}DO.id}。
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
     * 按主键删除，返回受影响行数。
     *
     * @param id 主键
     * @return 受影响行数
     */
    int deleteById(Long id);
}
