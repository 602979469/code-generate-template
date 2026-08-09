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
    ${className} findById(Long id);

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
     * 新增。
     *
     * @param ${classNameLower} ${entityName}
     * @return 新增后的${entityName}（主键已回填）
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
    void deleteById(Long id);
}
