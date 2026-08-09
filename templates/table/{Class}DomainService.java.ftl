package ${basePackage}.core.service;

import ${basePackage}.common.util.tools.${projectPrefix}Invoker;
import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.enums.ErrorCodeEnum;
import ${basePackage}.core.repository.${className}Repository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    public ${className} create${className}(${className} ${classNameLower}) {
<#list requiredColumns as c><#if c.string>        ${projectPrefix}Invoker.throwErrWhenBlank(${classNameLower}.get${c.propertyName?cap_first}(), ErrorCodeEnum.PARAM_INVALID, "${c.comment}不能为空");
<#else>        ${projectPrefix}Invoker.throwErrWhenNull(${classNameLower}.get${c.propertyName?cap_first}(), ErrorCodeEnum.PARAM_INVALID, "${c.comment}不能为空");
</#if></#list>        LocalDateTime now = LocalDateTime.now();
        ${classNameLower}.setCreateTime(now);
        ${classNameLower}.setUpdateTime(now);
        return ${classNameLower}Repository.insert(${classNameLower});
    }

    public ${className} update${className}(${className} ${classNameLower}) {
        ${projectPrefix}Invoker.throwErrWhenNull(${classNameLower}Repository.findById(${classNameLower}.getId()), ErrorCodeEnum.RESOURCE_NOT_FOUND);
        ${classNameLower}.setUpdateTime(LocalDateTime.now());
        return ${classNameLower}Repository.update(${classNameLower});
    }

    public void delete${className}(Long id) {
        ${projectPrefix}Invoker.throwErrWhenNull(${classNameLower}Repository.findById(id), ErrorCodeEnum.RESOURCE_NOT_FOUND);
        ${classNameLower}Repository.deleteById(id);
    }

    public ${className} get${className}(Long id) {
        ${className} ${classNameLower} = ${classNameLower}Repository.findById(id);
        ${projectPrefix}Invoker.throwErrWhenNull(${classNameLower}, ErrorCodeEnum.RESOURCE_NOT_FOUND);
        return ${classNameLower};
    }
}
