package ${basePackage}.core.repository;

import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.param.${className}QueryParam;
import ${basePackage}.core.model.result.PageResult;

import java.util.List;

/**
 * ${entityName}仓储：封装 Mapper，对外只暴露领域模型。当前阶段单表操作不引入事务。
 */
public interface ${className}Repository {

    /**
     * 按主键查询。
     *
     * @param id 主键
     * @return ${entityName}领域模型
     */
    ${className} findById(${pkJavaType} id);

    /**
     * 分页查询。
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<${className}> findPage(${className}QueryParam query);

    /**
     * 列表查询。
     *
     * @param query 查询参数
     * @return ${entityName}列表
     */
    List<${className}> findList(${className}QueryParam query);

    /**
     * 按条件查询单条：基于 {@code findList} 的结果集判断，不新增 Mapper 方法。
     *
     * @param query 查询参数
     * @return ${entityName}领域模型；未查询到返回 null，结果多于 1 条抛「查询结果不唯一」
     */
    ${className} findOne(${className}QueryParam query);

    /**
     * 新增。
     *
     * @param ${classNameLower} ${entityName}
     * @return 新增后的${entityName}；主键已回填到入参，返回同一对象
     */
    ${className} insert(${className} ${classNameLower});

    /**
     * 更新。
     *
     * @param ${classNameLower} ${entityName}
     */
    void update(${className} ${classNameLower});

    /**
     * 按条件更新：只更新传入的非空字段（部分更新）。
     * 注意：无法把字段更新为 null，需要置 null 请用 {@link #update}；create_time/update_time 由数据库自动维护。
     *
     * @param ${classNameLower} ${entityName}（至少含主键）
     */
    void updateByCondition(${className} ${classNameLower});

    /**
     * 按主键删除。
     *
     * @param id 主键
     */
    void deleteById(${pkJavaType} id);
}
