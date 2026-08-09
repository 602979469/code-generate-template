package ${basePackage}.core.service;

import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.param.${className}QueryParam;
import ${basePackage}.core.model.result.PageResult;

import java.util.List;

/**
 * ${entityName}领域服务
 *
 * 实现类为 ${className}ServiceImpl（core.service.impl 包）。
 */
public interface ${className}Service {

    /**
     * 创建${entityName}
     *
     * @param ${classNameLower} ${entityName}
     * @return 创建后的${entityName}（主键已回填）
     */
    ${className} create${className}(${className} ${classNameLower});

    /**
     * 更新${entityName}（全量）
     *
     * @param ${classNameLower} ${entityName}（含主键）
     */
    void update${className}(${className} ${classNameLower});

    /**
     * 按条件更新${entityName}（只更新传入的非空字段）。
     *
     * @param ${classNameLower} ${entityName}（至少含主键）
     */
    void updateByCondition(${className} ${classNameLower});

    /**
     * 删除${entityName}
     *
     * @param id ${entityName} ID
     */
    void delete${className}(Long id);

    /**
     * 按 ID 获取${entityName}
     *
     * @param id ${entityName} ID
     * @return ${entityName}
     */
    ${className} get${className}(Long id);

    /**
     * 分页查询${entityName}
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<${className}> findPage(${className}QueryParam query);

    /**
     * 列表查询${entityName}
     *
     * @param query 查询参数
     * @return ${entityName}列表
     */
    List<${className}> findList(${className}QueryParam query);
}
