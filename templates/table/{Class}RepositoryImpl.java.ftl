package ${basePackage}.core.repository.impl;

import ${basePackage}.common.dal.dataobject.${className}DO;
import ${basePackage}.common.dal.mapper.${className}Mapper;
import ${basePackage}.common.dal.query.${className}DalQuery;
import ${basePackage}.common.util.enums.LogFileEnum;
import ${basePackage}.common.util.result.PageResult;
import ${basePackage}.common.util.tools.AssertUtil;
import ${basePackage}.common.util.tools.ConvertUtil;
import ${basePackage}.common.util.tools.LoggerUtil;
import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.enums.ErrorCodeEnum;
import ${basePackage}.core.model.param.${className}QueryParam;
import ${basePackage}.core.repository.${className}Repository;
import ${basePackage}.core.repository.convertor.${className}Convertor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ${entityName}仓储：封装 Mapper，对外只暴露领域模型。单表操作不引入事务，多写事务由 core-service 编排。
 */
@Repository
public class ${className}RepositoryImpl implements ${className}Repository {

    /** ${entityName} Mapper。 */
    private final ${className}Mapper ${classNameLower}Mapper;

    public ${className}RepositoryImpl(${className}Mapper ${classNameLower}Mapper) {
        this.${classNameLower}Mapper = ${classNameLower}Mapper;
    }

    @Override
    public ${className} findBy${pkMethodName}(${pkMethodArgs}) {
        ${className}DO ${classNameLower}DO = ${classNameLower}Mapper.selectBy${pkMethodName}(${pkCallArgs});
        return ${className}Convertor.toModel(${classNameLower}DO);
    }

    @Override
    public List<${className}> findList(${className}QueryParam query) {
        ${className}DalQuery dalQuery = ${className}Convertor.toDalQuery(query);
        List<${className}DO> doList = ${classNameLower}Mapper.selectList(dalQuery);
        return ConvertUtil.map(doList, ${className}Convertor::toModel);
    }

    @Override
    public ${className} findOne(${className}QueryParam query) {
        ${className}DalQuery dalQuery = ${className}Convertor.toDalQuery(query);
        List<${className}DO> doList = ${classNameLower}Mapper.selectList(dalQuery);
        AssertUtil.throwErrWhenTrue(doList.size() > 1, ErrorCodeEnum.RESULT_NOT_UNIQUE,
                "查询结果不唯一：预期 1 条，实际 " + doList.size() + " 条");
        return doList.isEmpty() ? null : ${className}Convertor.toModel(doList.get(0));
    }

    @Override
    public PageResult<${className}> findPage(${className}QueryParam query) {
        ${className}DalQuery dalQuery = ${className}Convertor.toDalQuery(query);
        List<${className}DO> doList = ${classNameLower}Mapper.selectPage(dalQuery);
        long total = ${classNameLower}Mapper.countByQuery(dalQuery);
        List<${className}> list = ConvertUtil.map(doList, ${className}Convertor::toModel);
        return new PageResult<>(total, query.getPageNum(), query.getPageSize(), list);
    }

    @Override
    public ${className} insert(${className} ${classNameLower}) {
        ${className}DO ${classNameLower}DO = ${className}Convertor.toDO(${classNameLower});
        ${classNameLower}Mapper.insert(${classNameLower}DO);
<#if !compositePk>
        // 主键回填到入参（自增主键由数据库生成），调用方直接使用原对象
        ${classNameLower}.set${pkPropertyName?cap_first}(${classNameLower}DO.get${pkPropertyName?cap_first}());
</#if>
        return ${classNameLower};
    }

    @Override
    public int update(${className} ${classNameLower}) {
        ${className}DO ${classNameLower}DO = ${className}Convertor.toDO(${classNameLower});
        int affected = ${classNameLower}Mapper.update(${classNameLower}DO);
        LoggerUtil.info(LogFileEnum.BIZ_SERVICE, "${className}Repository.update ${pkPropertyName}={} 影响行数={}",
                ${classNameLower}.get${pkPropertyName?cap_first}(), affected);
        return affected;
    }

    @Override
    public int updateByCondition(${className} ${classNameLower}) {
        int affected = ${classNameLower}Mapper.updateByCondition(${className}Convertor.toDO(${classNameLower}));
        LoggerUtil.info(LogFileEnum.BIZ_SERVICE, "${className}Repository.updateByCondition ${pkPropertyName}={} 影响行数={}",
                ${classNameLower}.get${pkPropertyName?cap_first}(), affected);
        return affected;
    }

    @Override
    public int deleteBy${pkMethodName}(${pkMethodArgs}) {
        int affected = ${classNameLower}Mapper.deleteBy${pkMethodName}(${pkCallArgs});
        LoggerUtil.info(LogFileEnum.BIZ_SERVICE, "${className}Repository.deleteBy${pkMethodName} ${pkLogKey}={} 影响行数={}",
                ${pkLogFirstArg}, affected);
        return affected;
    }
}
