package ${basePackage}.biz.service;

import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.param.${className}QueryParam;
import ${basePackage}.core.model.result.PageResult;

import java.util.List;

/**
 * ${tableComment} Manager：用例编排，输入输出都是领域模型，不做前端格式转换。
 * 实现类为 ${className}ManagerImpl（biz.service.impl 包），web 层依赖本接口。
 */
public interface ${className}Manager {

    /**
     * 创建${entityName}。
     *
     * @param ${classNameLower} ${entityName}
     * @return 创建成功后的${entityName}
     */
    ${className} create${className}(${className} ${classNameLower});

    /**
     * 按 ID 查询${entityName}。
     *
     * @param id ${entityName} ID
     * @return ${entityName}
     */
    ${className} get${className}(Long id);

    /**
     * 分页查询${entityName}。
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<${className}> page${className}s(${className}QueryParam query);

    /**
     * 列表查询${entityName}。
     * 预留能力：web 未接线，业务方按需暴露。
     *
     * @param query 查询参数
     * @return ${entityName}列表
     */
    List<${className}> list${className}s(${className}QueryParam query);

    /**
     * 更新${entityName}（全量）。
     * 注意：PUT 为全量覆盖，未传字段会被置 NULL；部分更新请用 {@link #updateByCondition}。
     *
     * @param ${classNameLower} ${entityName}（含主键）
     * @return 更新后的${entityName}
     */
    ${className} update${className}(${className} ${classNameLower});

    /**
     * 按条件更新${entityName}（只更新传入的非空字段）。
     * 预留能力：web 未接线，业务方按需暴露。
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
