package ${basePackage}.core.service;

import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.param.${className}QueryParam;
import ${basePackage}.core.model.result.PageResult;

import java.util.List;

/**
 * ${tableComment}领域服务：承载${tableComment}相关的业务规则。只写规则，不碰持久化细节。
 * 实现类为 ${className}DomainServiceImpl（core.service.impl 包）。
 */
public interface ${className}DomainService {

    /**
     * 创建${entityName}：必填字段校验后入库。
     * createTime/updateTime 由数据库自动维护，领域层不赋值。
     *
     * @param ${classNameLower} ${entityName}
     * @return 创建后的${entityName}（主键已回填）
     */
    ${className} create${className}(${className} ${classNameLower});

    /**
     * 更新${entityName}（全量）：存在性校验后更新。
     *
     * @param ${classNameLower} ${entityName}（含主键）
     * @return 更新后的${entityName}
     */
    ${className} update${className}(${className} ${classNameLower});

    /**
     * 按条件更新${entityName}（只更新传入的非空字段）。
     * 全部业务字段均为空时跳过更新（不执行 SQL）。
     *
     * @param ${classNameLower} ${entityName}（至少含主键）
     */
    void updateByCondition(${className} ${classNameLower});

    /**
     * 删除${entityName}：存在性校验后删除。
     *
     * @param id ${entityName} ID
     */
    void delete${className}(Long id);

    /**
     * 按 ID 获取${entityName}：不存在时抛业务异常。
     *
     * @param id ${entityName} ID
     * @return ${entityName}
     */
    ${className} get${className}(Long id);

    /**
     * 分页查询${entityName}：纯查询，无规则。
     *
     * @param query 查询参数
     * @return 分页结果
     */
    PageResult<${className}> findPage(${className}QueryParam query);

    /**
     * 列表查询${entityName}：纯查询，无规则。
     *
     * @param query 查询参数
     * @return ${entityName}列表
     */
    List<${className}> findList(${className}QueryParam query);
}
