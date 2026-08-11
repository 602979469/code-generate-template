package ${basePackage}.biz.service.impl;

import ${basePackage}.biz.service.${className}Manager;
import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.enums.LogFileEnum;
import ${basePackage}.core.model.param.${className}QueryParam;
import ${basePackage}.core.model.result.PageResult;
import ${basePackage}.core.model.util.${toolPrefix}LoggerUtil;
import ${basePackage}.core.service.${className}Service;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ${entityName}管理实现类
 *
 */
@Service
public class ${className}ManagerImpl implements ${className}Manager {

    /** ${entityName}领域服务。 */
    private final ${className}Service ${classNameLower}Service;

    public ${className}ManagerImpl(${className}Service ${classNameLower}Service) {
        this.${classNameLower}Service = ${classNameLower}Service;
    }

    @Override
    public ${className} create${className}(${className} ${classNameLower}) {
        ${className} created = ${classNameLower}Service.create${className}(${classNameLower});
        ${toolPrefix}LoggerUtil.info(LogFileEnum.BIZ_SERVICE, "创建${entityName}成功 ${pkPropertyName}={}", created.get${pkPropertyName?cap_first}());
        return created;
    }

    @Override
    public ${className} get${className}(${pkJavaType} id) {
        return ${classNameLower}Service.get${className}(id);
    }

    @Override
    public PageResult<${className}> page${className}s(${className}QueryParam query) {
        return ${classNameLower}Service.findPage(query);
    }

    @Override
    public List<${className}> list${className}s(${className}QueryParam query) {
        return ${classNameLower}Service.findList(query);
    }

    @Override
    public void update${className}(${className} ${classNameLower}) {
        ${classNameLower}Service.update${className}(${classNameLower});
        ${toolPrefix}LoggerUtil.info(LogFileEnum.BIZ_SERVICE, "更新${entityName}成功 ${pkPropertyName}={}", ${classNameLower}.get${pkPropertyName?cap_first}());
    }

    @Override
    public void updateByCondition(${className} ${classNameLower}) {
        ${classNameLower}Service.updateByCondition(${classNameLower});
        ${toolPrefix}LoggerUtil.info(LogFileEnum.BIZ_SERVICE, "按条件更新${entityName}成功 ${pkPropertyName}={}", ${classNameLower}.get${pkPropertyName?cap_first}());
    }

    @Override
    public void delete${className}(${pkJavaType} id) {
        ${classNameLower}Service.delete${className}(id);
        ${toolPrefix}LoggerUtil.info(LogFileEnum.BIZ_SERVICE, "删除${entityName}成功 id={}", id);
    }
}
