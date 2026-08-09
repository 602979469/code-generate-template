package ${basePackage}.core.repository;

import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.param.${className}QueryParam;
import ${basePackage}.core.model.result.PageResult;

/**
 * ${tableComment}仓储：封装 Mapper，对外只暴露领域模型。当前阶段单表操作不引入事务。
 */
public interface ${className}Repository {

    ${className} findById(Long id);

    PageResult<${className}> findPage(${className}QueryParam query);

    ${className} insert(${className} ${classNameLower});

    ${className} update(${className} ${classNameLower});

    void deleteById(Long id);
}
