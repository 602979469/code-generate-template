package ${basePackage}.app.biz;

import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.param.${className}QueryParam;
import ${basePackage}.core.model.result.PageResult;
import ${basePackage}.core.service.${className}DomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ${tableComment} Manager：用例编排。输入输出都是领域模型，不做前端格式转换。
 * 只依赖 core-model 与 core-service（DomainService），不直接触碰仓储。
 */
@Service
public class ${className}Manager {

    private static final Logger log = LoggerFactory.getLogger(${className}Manager.class);

    /** ${tableComment}领域服务。 */
    private final ${className}DomainService ${classNameLower}DomainService;

    public ${className}Manager(${className}DomainService ${classNameLower}DomainService) {
        this.${classNameLower}DomainService = ${classNameLower}DomainService;
    }

    /**
     * 创建${entityName}。
     *
     * @param ${classNameLower} ${entityName}
     * @return 创建成功后的${entityName}
     */
    public ${className} create${className}(${className} ${classNameLower}) {
        ${className} created = ${classNameLower}DomainService.create${className}(${classNameLower});
        log.info("创建${entityName}成功 id={}", created.getId());
        return created;
    }

    /**
     * 按 ID 查询${entityName}。
     *
     * @param id ${entityName} ID
     * @return ${entityName}
     */
    public ${className} get${className}(Long id) {
        return ${classNameLower}DomainService.get${className}(id);
    }

    /**
     * 分页查询${entityName}。
     *
     * @param query 查询参数
     * @return 分页结果
     */
    public PageResult<${className}> page${className}s(${className}QueryParam query) {
        return ${classNameLower}DomainService.findPage(query);
    }

    /**
     * 列表查询${entityName}。
     * 预留能力：web 未接线，业务方按需暴露。
     *
     * @param query 查询参数
     * @return ${entityName}列表
     */
    public List<${className}> list${className}s(${className}QueryParam query) {
        return ${classNameLower}DomainService.findList(query);
    }

    /**
     * 更新${entityName}（全量）。
     * 注意：PUT 为全量覆盖，未传字段会被置 NULL；部分更新请用 {@link #updateByCondition}。
     *
     * @param ${classNameLower} ${entityName}（含主键）
     * @return 更新后的${entityName}
     */
    public ${className} update${className}(${className} ${classNameLower}) {
        ${className} updated = ${classNameLower}DomainService.update${className}(${classNameLower});
        log.info("更新${entityName}成功 id={}", updated.getId());
        return updated;
    }

    /**
     * 按条件更新${entityName}（只更新传入的非空字段）。
     * 预留能力：web 未接线，业务方按需暴露。
     *
     * @param ${classNameLower} ${entityName}（至少含主键）
     */
    public void updateByCondition(${className} ${classNameLower}) {
        ${classNameLower}DomainService.updateByCondition(${classNameLower});
        log.info("按条件更新${entityName}成功 id={}", ${classNameLower}.getId());
    }

    /**
     * 删除${entityName}。
     *
     * @param id ${entityName} ID
     */
    public void delete${className}(Long id) {
        ${classNameLower}DomainService.delete${className}(id);
        log.info("删除${entityName}成功 id={}", id);
    }
}
