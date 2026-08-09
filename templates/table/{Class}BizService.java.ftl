package ${basePackage}.app.biz;

import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.param.${className}QueryParam;
import ${basePackage}.core.model.result.PageResult;
import ${basePackage}.core.repository.${className}Repository;
import ${basePackage}.core.service.${className}DomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * ${tableComment}业务服务：用例编排。输入输出都是领域模型，不做前端格式转换。
 */
@Service
public class ${className}BizService {

    private static final Logger log = LoggerFactory.getLogger(${className}BizService.class);

    private final ${className}DomainService ${classNameLower}DomainService;

    private final ${className}Repository ${classNameLower}Repository;

    public ${className}BizService(${className}DomainService ${classNameLower}DomainService,
                                  ${className}Repository ${classNameLower}Repository) {
        this.${classNameLower}DomainService = ${classNameLower}DomainService;
        this.${classNameLower}Repository = ${classNameLower}Repository;
    }

    public ${className} create${className}(${className} ${classNameLower}) {
        ${className} created = ${classNameLower}DomainService.create${className}(${classNameLower});
        log.info("创建${tableComment}成功 id={}", created.getId());
        return created;
    }

    public ${className} get${className}(Long id) {
        return ${classNameLower}DomainService.get${className}(id);
    }

    public PageResult<${className}> page${className}s(${className}QueryParam query) {
        return ${classNameLower}Repository.findPage(query);
    }

    public ${className} update${className}(${className} ${classNameLower}) {
        ${className} updated = ${classNameLower}DomainService.update${className}(${classNameLower});
        log.info("更新${tableComment}成功 id={}", updated.getId());
        return updated;
    }

    public void delete${className}(Long id) {
        ${classNameLower}DomainService.delete${className}(id);
        log.info("删除${tableComment}成功 id={}", id);
    }
}
