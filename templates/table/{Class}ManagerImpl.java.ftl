package ${basePackage}.biz.service.impl;

import ${basePackage}.biz.service.${className}Manager;
import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.enums.LogFileEnum;
import ${basePackage}.core.model.param.${className}QueryParam;
import ${basePackage}.core.model.result.PageResult;
import ${basePackage}.core.model.util.${toolPrefix}LoggerUtil;
import ${basePackage}.core.service.${className}DomainService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ${tableComment} Manager 实现：用例编排，只依赖 core-model 与 core-service（DomainService），不直接触碰仓储。
 */
@Service
public class ${className}ManagerImpl implements ${className}Manager {

    /** ${tableComment}领域服务。 */
    private final ${className}DomainService ${classNameLower}DomainService;

    public ${className}ManagerImpl(${className}DomainService ${classNameLower}DomainService) {
        this.${classNameLower}DomainService = ${classNameLower}DomainService;
    }

    @Override
    public ${className} create${className}(${className} ${classNameLower}) {
        ${className} created = ${classNameLower}DomainService.create${className}(${classNameLower});
        ${toolPrefix}LoggerUtil.info(LogFileEnum.BIZ_SERVICE, "创建${entityName}成功 id={}", created.getId());
        return created;
    }

    @Override
    public ${className} get${className}(Long id) {
        return ${classNameLower}DomainService.get${className}(id);
    }

    @Override
    public PageResult<${className}> page${className}s(${className}QueryParam query) {
        return ${classNameLower}DomainService.findPage(query);
    }

    @Override
    public List<${className}> list${className}s(${className}QueryParam query) {
        return ${classNameLower}DomainService.findList(query);
    }

    @Override
    public ${className} update${className}(${className} ${classNameLower}) {
        ${className} updated = ${classNameLower}DomainService.update${className}(${classNameLower});
        ${toolPrefix}LoggerUtil.info(LogFileEnum.BIZ_SERVICE, "更新${entityName}成功 id={}", updated.getId());
        return updated;
    }

    @Override
    public void updateByCondition(${className} ${classNameLower}) {
        ${classNameLower}DomainService.updateByCondition(${classNameLower});
        ${toolPrefix}LoggerUtil.info(LogFileEnum.BIZ_SERVICE, "按条件更新${entityName}成功 id={}", ${classNameLower}.getId());
    }

    @Override
    public void delete${className}(Long id) {
        ${classNameLower}DomainService.delete${className}(id);
        ${toolPrefix}LoggerUtil.info(LogFileEnum.BIZ_SERVICE, "删除${entityName}成功 id={}", id);
    }
}
