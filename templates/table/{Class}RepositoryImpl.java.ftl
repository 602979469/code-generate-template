package ${basePackage}.core.repository.impl;

import ${basePackage}.common.dal.dataobject.${className}DO;
import ${basePackage}.common.dal.mapper.${className}Mapper;
import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.param.${className}QueryParam;
import ${basePackage}.core.model.result.PageResult;
import ${basePackage}.core.repository.${className}Repository;
import ${basePackage}.core.repository.convertor.${className}Convertor;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

import static ${basePackage}.core.repository.convertor.${className}Convertor.toDO;
import static ${basePackage}.core.repository.convertor.${className}Convertor.toModel;

/**
 * ${tableComment}仓储：封装 Mapper，对外只暴露领域模型。当前阶段单表操作不引入事务。
 */
@Repository
public class ${className}RepositoryImpl implements ${className}Repository {

    @Resource
    private ${className}Mapper ${classNameLower}Mapper;

    public ${className} findById(Long id) {
        return toModel(${classNameLower}Mapper.selectById(id));
    }

    public PageResult<${className}> findPage(${className}QueryParam query) {
        List<${className}DO> doList = ${classNameLower}Mapper.selectPage(query);
        long total = ${classNameLower}Mapper.countByQuery(query);
        List<${className}> list = doList.stream().map(${className}Convertor::toModel).toList();
        return new PageResult<>(total, query.getPageNum(), query.getPageSize(), list);
    }

    public ${className} insert(${className} ${classNameLower}) {
        ${className}DO ${classNameLower}DO = toDO(${classNameLower});
        ${classNameLower}Mapper.insert(${classNameLower}DO);
        return toModel(${classNameLower}DO);
    }

    public ${className} update(${className} ${classNameLower}) {
        ${className}DO ${classNameLower}DO = toDO(${classNameLower});
        ${classNameLower}Mapper.update(${classNameLower}DO);
        return toModel(${classNameLower}DO);
    }

    public void deleteById(Long id) {
        ${classNameLower}Mapper.deleteById(id);
    }
}
