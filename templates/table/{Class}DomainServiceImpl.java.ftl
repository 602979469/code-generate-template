package ${basePackage}.core.service.impl;

import ${basePackage}.common.util.tools.${toolPrefix}Invoker;
import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.enums.ErrorCodeEnum;
import ${basePackage}.core.model.param.${className}QueryParam;
import ${basePackage}.core.model.result.PageResult;
import ${basePackage}.core.repository.${className}Repository;
import ${basePackage}.core.service.${className}DomainService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ${tableComment}领域服务实现：承载${tableComment}相关的业务规则。只写规则，不碰持久化细节。
 */
@Service
public class ${className}DomainServiceImpl implements ${className}DomainService {

    /** ${tableComment}仓储。 */
    private final ${className}Repository ${classNameLower}Repository;

    public ${className}DomainServiceImpl(${className}Repository ${classNameLower}Repository) {
        this.${classNameLower}Repository = ${classNameLower}Repository;
    }

    @Override
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

    @Override
    public ${className} update${className}(${className} ${classNameLower}) {
        ${toolPrefix}Invoker.throwErrWhenNull(
                ${classNameLower}Repository.findById(${classNameLower}.getId()),
                ErrorCodeEnum.RESOURCE_NOT_FOUND);
        return ${classNameLower}Repository.update(${classNameLower});
    }

    @Override
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

    @Override
    public void delete${className}(Long id) {
        ${toolPrefix}Invoker.throwErrWhenNull(
                ${classNameLower}Repository.findById(id),
                ErrorCodeEnum.RESOURCE_NOT_FOUND);
        ${classNameLower}Repository.deleteById(id);
    }

    @Override
    public ${className} get${className}(Long id) {
        ${className} ${classNameLower} = ${classNameLower}Repository.findById(id);
        ${toolPrefix}Invoker.throwErrWhenNull(
                ${classNameLower},
                ErrorCodeEnum.RESOURCE_NOT_FOUND);
        return ${classNameLower};
    }

    @Override
    public PageResult<${className}> findPage(${className}QueryParam query) {
        return ${classNameLower}Repository.findPage(query);
    }

    @Override
    public List<${className}> findList(${className}QueryParam query) {
        return ${classNameLower}Repository.findList(query);
    }
}
