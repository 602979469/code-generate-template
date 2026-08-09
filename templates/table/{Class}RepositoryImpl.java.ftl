package ${basePackage}.core.repository.impl;

import ${basePackage}.common.dal.dataobject.${className}DO;
import ${basePackage}.common.dal.mapper.${className}Mapper;
import ${basePackage}.common.util.tools.${toolPrefix}Invoker;
import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.enums.ErrorCodeEnum;
import ${basePackage}.core.model.enums.LogFileEnum;
import ${basePackage}.core.model.param.${className}QueryParam;
import ${basePackage}.core.model.result.PageResult;
import ${basePackage}.core.model.util.${toolPrefix}LoggerUtil;
import ${basePackage}.core.repository.${className}Repository;
import ${basePackage}.core.repository.convertor.${className}Convertor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ${entityName}仓储：封装 Mapper，对外只暴露领域模型。当前阶段单表操作不引入事务。
 */
@Repository
public class ${className}RepositoryImpl implements ${className}Repository {

    /** ${entityName} Mapper。 */
    private final ${className}Mapper ${classNameLower}Mapper;

    public ${className}RepositoryImpl(${className}Mapper ${classNameLower}Mapper) {
        this.${classNameLower}Mapper = ${classNameLower}Mapper;
    }

    @Override
    public ${className} findById(Long id) {
        return ${className}Convertor.toModel(${classNameLower}Mapper.selectById(id));
    }

    @Override
    public List<${className}> findList(${className}QueryParam query) {
        return ${classNameLower}Mapper.selectList(query).stream().map(${className}Convertor::toModel).toList();
    }

    @Override
    public PageResult<${className}> findPage(${className}QueryParam query) {
        List<${className}DO> doList = ${classNameLower}Mapper.selectPage(query);
        long total = ${classNameLower}Mapper.countByQuery(query);
        List<${className}> list = doList.stream().map(${className}Convertor::toModel).toList();
        return new PageResult<>(total, query.getPageNum(), query.getPageSize(), list);
    }

    @Override
    public ${className} insert(${className} ${classNameLower}) {
        ${className}DO ${classNameLower}DO = ${className}Convertor.toDO(${classNameLower});
        ${classNameLower}Mapper.insert(${classNameLower}DO);
        return ${className}Convertor.toModel(${classNameLower}DO);
    }

    @Override
    public void update(${className} ${classNameLower}) {
        ${className}DO ${classNameLower}DO = ${className}Convertor.toDO(${classNameLower});
        int affected = ${classNameLower}Mapper.update(${classNameLower}DO);
        ${toolPrefix}LoggerUtil.info(LogFileEnum.BIZ_SERVICE, "${className}Repository.update id={} 影响行数={}", ${classNameLower}.getId(), affected);
        ${toolPrefix}Invoker.throwErrWhenTrue(affected == 0, ErrorCodeEnum.UPDATE_FAILED, "更新失败：记录不存在或已被修改");
    }

    @Override
    public void updateByCondition(${className} ${classNameLower}) {
        int affected = ${classNameLower}Mapper.updateByCondition(${className}Convertor.toDO(${classNameLower}));
        ${toolPrefix}LoggerUtil.info(LogFileEnum.BIZ_SERVICE, "${className}Repository.updateByCondition id={} 影响行数={}", ${classNameLower}.getId(), affected);
        ${toolPrefix}Invoker.throwErrWhenTrue(affected == 0, ErrorCodeEnum.UPDATE_FAILED, "更新失败：记录不存在或已被修改");
    }

    @Override
    public void deleteById(Long id) {
        int affected = ${classNameLower}Mapper.deleteById(id);
        ${toolPrefix}LoggerUtil.info(LogFileEnum.BIZ_SERVICE, "${className}Repository.deleteById id={} 影响行数={}", id, affected);
        ${toolPrefix}Invoker.throwErrWhenTrue(affected == 0, ErrorCodeEnum.DELETE_FAILED, "删除失败：记录不存在或已被删除");
    }
}
