package ${basePackage}.app.biz;

import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.param.${className}QueryParam;
import ${basePackage}.core.model.result.PageResult;
import ${basePackage}.core.repository.${className}Repository;
import ${basePackage}.core.service.${className}DomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ${tableComment}业务服务：用例编排。输入输出都是领域模型，不做前端格式转换。
 */
@Service
public class ${className}BizService {

    private static final Logger log = LoggerFactory.getLogger(${className}BizService.class);

    /** ${tableComment}领域服务。 */
    private final ${className}DomainService ${classNameLower}DomainService;

    /** ${tableComment}仓储。 */
    private final ${className}Repository ${classNameLower}Repository;

    public ${className}BizService(${className}DomainService ${classNameLower}DomainService,
                                  ${className}Repository ${classNameLower}Repository) {
        this.${classNameLower}DomainService = ${classNameLower}DomainService;
        this.${classNameLower}Repository = ${classNameLower}Repository;
    }

    /**
     * 创建${tableComment}。
     *
     * @param ${classNameLower} ${tableComment}
     * @return 创建成功后的${tableComment}
     */
    public ${className} create${className}(${className} ${classNameLower}) {
        ${className} created = ${classNameLower}DomainService.create${className}(${classNameLower});
        log.info("创建${tableComment}成功 id={}", created.getId());
        return created;
    }

    /**
     * 按 ID 查询${tableComment}。
     *
     * @param id ${tableComment} ID
     * @return ${tableComment}
     */
    public ${className} get${className}(Long id) {
        return ${classNameLower}DomainService.get${className}(id);
    }

    /**
     * 分页查询${tableComment}。
     *
     * @param query 查询参数
     * @return 分页结果
     */
    public PageResult<${className}> page${className}s(${className}QueryParam query) {
        return ${classNameLower}Repository.findPage(query);
    }

    /**
     * 列表查询${tableComment}。
     *
     * @param query 查询参数
     * @return ${tableComment}列表
     */
    public List<${className}> list${className}s(${className}QueryParam query) {
        return ${classNameLower}Repository.findList(query);
    }

    /**
     * 更新${tableComment}（全量）。
     *
     * @param ${classNameLower} ${tableComment}（含主键）
     * @return 更新后的${tableComment}
     */
    public ${className} update${className}(${className} ${classNameLower}) {
        ${className} updated = ${classNameLower}DomainService.update${className}(${classNameLower});
        log.info("更新${tableComment}成功 id={}", updated.getId());
        return updated;
    }

    /**
     * 按条件更新${tableComment}（只更新传入的非空字段）。
     *
     * @param ${classNameLower} ${tableComment}（至少含主键）
     */
    public void updateByCondition(${className} ${classNameLower}) {
        ${classNameLower}Repository.updateByCondition(${classNameLower});
        log.info("按条件更新${tableComment}成功 id={}", ${classNameLower}.getId());
    }

    /**
     * 删除${tableComment}。
     *
     * @param id ${tableComment} ID
     */
    public void delete${className}(Long id) {
        ${classNameLower}DomainService.delete${className}(id);
        log.info("删除${tableComment}成功 id={}", id);
    }
}
