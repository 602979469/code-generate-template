package ${basePackage}.core.service;

import ${basePackage}.common.util.tools.${toolPrefix}Invoker;
import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.enums.ErrorCodeEnum;
import ${basePackage}.core.repository.${className}Repository;
import org.springframework.stereotype.Service;

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
     * 创建${tableComment}：必填字段校验后入库。
     * createTime/updateTime 由数据库自动维护，领域层不赋值。
     *
     * @param ${classNameLower} ${tableComment}
     * @return 创建后的${tableComment}（主键已回填）
     */
    public ${className} create${className}(${className} ${classNameLower}) {
<#list requiredColumns as c><#if c.string>        ${toolPrefix}Invoker.throwErrWhenBlank(${classNameLower}.get${c.propertyName?cap_first}(), ErrorCodeEnum.PARAM_INVALID, "${c.comment}不能为空");
<#else>        ${toolPrefix}Invoker.throwErrWhenNull(${classNameLower}.get${c.propertyName?cap_first}(), ErrorCodeEnum.PARAM_INVALID, "${c.comment}不能为空");
</#if></#list>        return ${classNameLower}Repository.insert(${classNameLower});
    }

    /**
     * 更新${className}（全量）：存在性校验后更新。
     * updateTime 由数据库 ON UPDATE CURRENT_TIMESTAMP 自动维护。
     *
     * @param ${classNameLower} ${tableComment}（含主键）
     * @return 更新后的${tableComment}
     */
    public ${className} update${className}(${className} ${classNameLower}) {
        ${toolPrefix}Invoker.throwErrWhenNull(${classNameLower}Repository.findById(${classNameLower}.getId()), ErrorCodeEnum.RESOURCE_NOT_FOUND);
        return ${classNameLower}Repository.update(${classNameLower});
    }

    /**
     * 删除${tableComment}：存在性校验后删除。
     *
     * @param id ${tableComment} ID
     */
    public void delete${className}(Long id) {
        ${toolPrefix}Invoker.throwErrWhenNull(${classNameLower}Repository.findById(id), ErrorCodeEnum.RESOURCE_NOT_FOUND);
        ${classNameLower}Repository.deleteById(id);
    }

    /**
     * 按 ID 获取${tableComment}：不存在时抛业务异常。
     *
     * @param id ${tableComment} ID
     * @return ${tableComment}
     */
    public ${className} get${className}(Long id) {
        ${className} ${classNameLower} = ${classNameLower}Repository.findById(id);
        ${toolPrefix}Invoker.throwErrWhenNull(${classNameLower}, ErrorCodeEnum.RESOURCE_NOT_FOUND);
        return ${classNameLower};
    }
}
