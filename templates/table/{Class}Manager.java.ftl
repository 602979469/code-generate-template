package ${basePackage}.biz.service;

import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.param.${className}QueryParam;
import ${basePackage}.core.model.result.PageResult;

import java.util.List;

/**
 * ${entityName}管理类接口定义
 * 
 */
public interface ${className}Manager {

    /**
     * 创建${entityName}
     *
     * @param ${classNameLower} ${entityName}
     * @return 创建成功后的${entityName}
     */
    ${className} create${className}(${className} ${classNameLower});

    /**
     * 按 ID 查询${entityName}
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
    PageResult<${className}> page${className}s(${className}QueryParam query);

    /**
     * 列表查询${entityName}
     *
     * @param query 查询参数
     * @return ${entityName}列表
     */
    List<${className}> list${className}s(${className}QueryParam query);

    /**
     * 更新${entityName}（全量）。
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
     * 删除${entityName}。
     *
     * @param id ${entityName} ID
     */
    void delete${className}(Long id);
}
