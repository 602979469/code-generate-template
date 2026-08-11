package ${basePackage}.core.service.impl;

import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.param.${className}QueryParam;
import ${basePackage}.core.model.result.PageResult;
import ${basePackage}.core.repository.${className}Repository;
import ${basePackage}.core.service.${className}Service;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ${entityName}领域服务实现：承载${entityName}相关的业务规则。只写规则，不碰持久化细节。
 */
@Service
public class ${className}ServiceImpl implements ${className}Service {

    /** ${entityName}仓储。 */
    private final ${className}Repository ${classNameLower}Repository;

    public ${className}ServiceImpl(${className}Repository ${classNameLower}Repository) {
        this.${classNameLower}Repository = ${classNameLower}Repository;
    }

    @Override
    public ${className} create${className}(${className} ${classNameLower}) {
        return ${classNameLower}Repository.insert(${classNameLower});
    }

    @Override
    public void update${className}(${className} ${classNameLower}) {
        ${classNameLower}Repository.update(${classNameLower});
    }

    @Override
    public void updateByCondition(${className} ${classNameLower}) {
        ${classNameLower}Repository.updateByCondition(${classNameLower});
    }

    @Override
    public void delete${className}(${pkJavaType} id) {
        ${classNameLower}Repository.deleteById(id);
    }

    @Override
    public ${className} get${className}(${pkJavaType} id) {
        return ${classNameLower}Repository.findById(id);
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
