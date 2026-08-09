package ${basePackage}.core.repository.impl;

import ${basePackage}.common.dal.dataobject.${className}DO;
import ${basePackage}.common.dal.mapper.${className}Mapper;
import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.param.${className}QueryParam;
import ${basePackage}.core.model.result.PageResult;
import ${basePackage}.core.repository.${className}Repository;
import ${basePackage}.core.repository.assembler.${className}Assembler;
import org.springframework.stereotype.Repository;

import java.util.List;

import static ${basePackage}.core.repository.assembler.${className}Assembler.toDO;
import static ${basePackage}.core.repository.assembler.${className}Assembler.toModel;

/**
 * ${tableComment}仓储：封装 Mapper，对外只暴露领域模型。当前阶段单表操作不引入事务。
 */
@Repository
public class ${className}RepositoryImpl implements ${className}Repository {

    /** ${tableComment} Mapper。 */
    private final ${className}Mapper ${classNameLower}Mapper;

    public ${className}RepositoryImpl(${className}Mapper ${classNameLower}Mapper) {
        this.${classNameLower}Mapper = ${classNameLower}Mapper;
    }

    @Override
    public ${className} findById(Long id) {
        return toModel(${classNameLower}Mapper.selectById(id));
    }

    @Override
    public List<${className}> findList(${className}QueryParam query) {
        return ${classNameLower}Mapper.selectList(query).stream().map(${className}Assembler::toModel).toList();
    }

    @Override
    public PageResult<${className}> findPage(${className}QueryParam query) {
        List<${className}DO> doList = ${classNameLower}Mapper.selectPage(query);
        long total = ${classNameLower}Mapper.countByQuery(query);
        List<${className}> list = doList.stream().map(${className}Assembler::toModel).toList();
        return new PageResult<>(total, query.getPageNum(), query.getPageSize(), list);
    }

    @Override
    public ${className} insert(${className} ${classNameLower}) {
        ${className}DO ${classNameLower}DO = toDO(${classNameLower});
        ${classNameLower}Mapper.insert(${classNameLower}DO);
        return toModel(${classNameLower}DO);
    }

    @Override
    public ${className} update(${className} ${classNameLower}) {
        ${className}DO ${classNameLower}DO = toDO(${classNameLower});
        ${classNameLower}Mapper.update(${classNameLower}DO);
        return toModel(${classNameLower}DO);
    }

    @Override
    public void updateByCondition(${className} ${classNameLower}) {
        ${classNameLower}Mapper.updateByCondition(toDO(${classNameLower}));
    }

    @Override
    public void deleteById(Long id) {
        ${classNameLower}Mapper.deleteById(id);
    }
}
