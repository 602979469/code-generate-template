package ${pkgBizImpl};

import ${basePackage}.common.framework.enums.LogFileEnum;
import ${basePackage}.common.framework.result.PageResult;
import ${basePackage}.common.framework.tools.LoggerUtil;
import ${pkgBiz}.${className}Manager;
import ${pkgDomain}.${className};
import ${pkgParam}.${className}QueryParam;
import ${pkgService}.${className}Service;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ${entityName}管理实现类
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
        LoggerUtil.info(LogFileEnum.BIZ_SERVICE, "创建${entityName}成功 ${pkPropertyName}={}", created.get${pkPropertyName?cap_first}());
        return created;
    }

    @Override
    public ${className} get${className}(${pkMethodArgs}) {
        return ${classNameLower}Service.get${className}(${pkCallArgs});
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
    public int update${className}(${className} ${classNameLower}) {
        int affected = ${classNameLower}Service.update${className}(${classNameLower});
        LoggerUtil.info(LogFileEnum.BIZ_SERVICE, "更新${entityName}成功 ${pkPropertyName}={} 影响行数={}",
                ${classNameLower}.get${pkPropertyName?cap_first}(), affected);
        return affected;
    }

    @Override
    public int updateByCondition(${className} ${classNameLower}) {
        int affected = ${classNameLower}Service.updateByCondition(${classNameLower});
        LoggerUtil.info(LogFileEnum.BIZ_SERVICE, "按条件更新${entityName}成功 ${pkPropertyName}={} 影响行数={}",
                ${classNameLower}.get${pkPropertyName?cap_first}(), affected);
        return affected;
    }

    @Override
    public int delete${className}(${pkMethodArgs}) {
        int affected = ${classNameLower}Service.delete${className}(${pkCallArgs});
        LoggerUtil.info(LogFileEnum.BIZ_SERVICE, "删除${entityName}成功 ${pkLogKey}={} 影响行数={}", ${pkLogFirstArg}, affected);
        return affected;
    }
}
