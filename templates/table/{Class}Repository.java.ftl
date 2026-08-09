package ${basePackage}.core.repository;

import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.param.${className}QueryParam;
import ${basePackage}.core.model.result.PageResult;

/**
 * ${tableComment}仓储：封装 Mapper，对外只暴露领域模型。当前阶段单表操作不引入事务。
 */
public interface ${className}Repository {

    /**
     * 按主键查询。
     *
     * @param id 主键
     * @return ${tableComment}领域模型
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
     * 新增。
     *
     * @param ${classNameLower} ${tableComment}
     * @return 新增后的${tableComment}（主键已回填）
     */
    ${className} insert(${className} ${classNameLower});

    /**
     * 更新。
     *
     * @param ${classNameLower} ${tableComment}
     * @return 更新后的${tableComment}
     */
    ${className} update(${className} ${classNameLower});

    /**
     * 按主键删除。
     *
     * @param id 主键
     */
    void deleteById(Long id);
}
