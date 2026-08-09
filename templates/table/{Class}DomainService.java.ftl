package ${basePackage}.core.service;

import ${basePackage}.common.util.tools.${toolPrefix}Invoker;
import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.enums.ErrorCodeEnum;
import ${basePackage}.core.model.param.${className}QueryParam;
import ${basePackage}.core.model.result.PageResult;
import ${basePackage}.core.repository.${className}Repository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ${tableComment}领域服务：承载${tableComment}相关的业务规则。只写规则，不碰持久化细节。
 */
@Service
public class ${className}DomainService {

    /** ${tableComment}仓储。 */
    private final ${className}Repository ${classNameLower}Repository;

    public ${className}DomainService(${className}Repository ${classNameLower}Repository) {
        this.${classNameLower}Repository = ${classNameLower}Repository;
    }

    /**
     * 创建${entityName}：必填字段校验后入库。
     * createTime/updateTime 由数据库自动维护，领域层不赋值。
     *
     * @param ${classNameLower} ${entityName}
     * @return 创建后的${entityName}（主键已回填）
     */
    public ${className} create${className}(${className} ${classNameLower}) {
<#list requiredColumns as c><#if c.string>        ${toolPrefix}Invoker.throwErrWhenBlank(
                ${classNameLower}.get${c.propertyName?cap_first}(),
                ErrorCodeEnum.PARAM_INVALID,
                "${c.comment}不能为空");
<#else>        ${toolPrefix}Invoker.throwErrWhenNull(
                ${classNameLower}.get${c.propertyName?cap_first}(),
                ErrorCodeEnum.PARAM_INVALID,
                "${c.comment}不能为空");
</#if></#list>        return ${classNameLower}Repository.insert(${classNameLower});
    }

    /**
     * 更新${entityName}（全量）：存在性校验后更新。
     * updateTime 由数据库 ON UPDATE CURRENT_TIMESTAMP 自动维护。
     *
     * @param ${classNameLower} ${entityName}（含主键）
     * @return 更新后的${entityName}
     */
    public ${className} update${className}(${className} ${classNameLower}) {
        ${toolPrefix}Invoker.throwErrWhenNull(
                ${classNameLower}Repository.findById(${classNameLower}.getId()),
                ErrorCodeEnum.RESOURCE_NOT_FOUND);
        return ${classNameLower}Repository.update(${classNameLower});
    }

    /**
     * 按条件更新${entityName}（只更新传入的非空字段）。
     * 全部业务字段均为空时跳过更新（不执行 SQL）。
     *
     * @param ${classNameLower} ${entityName}（至少含主键）
     */
    public void updateByCondition(${className} ${classNameLower}) {
        ${toolPrefix}Invoker.throwErrWhenNull(
                ${classNameLower}Repository.findById(${classNameLower}.getId()),
                ErrorCodeEnum.RESOURCE_NOT_FOUND);
<#if columns?has_content>
        // 全部业务字段均为空时跳过更新，避免产生空 SQL
        if (<#list columns as c>${classNameLower}.get${c.propertyName?cap_first}() == null<#sep> && </#list>) {
            return;
        }
</#if>        ${classNameLower}Repository.updateByCondition(${classNameLower});
    }

    /**
     * 删除${entityName}：存在性校验后删除。
     *
     * @param id ${entityName} ID
     */
    public void delete${className}(Long id) {
        ${toolPrefix}Invoker.throwErrWhenNull(
                ${classNameLower}Repository.findById(id),
                ErrorCodeEnum.RESOURCE_NOT_FOUND);
        ${classNameLower}Repository.deleteById(id);
    }

    /**
     * 按 ID 获取${entityName}：不存在时抛业务异常。
     *
     * @param id ${entityName} ID
     * @return ${entityName}
     */
    public ${className} get${className}(Long id) {
        ${className} ${classNameLower} = ${classNameLower}Repository.findById(id);
        ${toolPrefix}Invoker.throwErrWhenNull(
                ${classNameLower},
                ErrorCodeEnum.RESOURCE_NOT_FOUND);
        return ${classNameLower};
    }

    /**
     * 分页查询${entityName}：纯查询，无规则。
     *
     * @param query 查询参数
     * @return 分页结果
     */
    public PageResult<${className}> findPage(${className}QueryParam query) {
        return ${classNameLower}Repository.findPage(query);
    }

    /**
     * 列表查询${entityName}：纯查询，无规则。
     *
     * @param query 查询参数
     * @return ${entityName}列表
     */
    public List<${className}> findList(${className}QueryParam query) {
        return ${classNameLower}Repository.findList(query);
    }
}
