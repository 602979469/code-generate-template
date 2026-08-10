package ${basePackage}.web.controller;

import cn.hutool.core.util.ObjectUtil;
import ${basePackage}.biz.service.${className}Manager;
import ${basePackage}.web.assembler.${className}Assembler;
import ${basePackage}.web.checker.${className}ParamChecker;
import ${basePackage}.web.param.${className}CreateRequest;
import ${basePackage}.web.param.${className}QueryRequest;
import ${basePackage}.web.param.${className}UpdateRequest;
import ${basePackage}.web.result.${className}Response;
import ${basePackage}.web.result.${toolPrefix}Result;
import ${basePackage}.web.template.${toolPrefix}Template;
import ${basePackage}.common.util.tools.${toolPrefix}Invoker;
import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.enums.ErrorCodeEnum;
import ${basePackage}.core.model.result.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ${entityName}管理接口。Controller 只做参数校验、DTO 转换与结果包装，不含业务规则；
 * 参数校验、异常封装、请求日志与 Result 组装统一交给 ${toolPrefix}Template。
 */
@RestController
@RequestMapping("/api/v1/${classNameLower}s")
@Tag(name = "${entityName}管理")
public class ${className}Controller {

    /** ${entityName} Manager。 */
    private final ${className}Manager ${classNameLower}Manager;

    public ${className}Controller(${className}Manager ${classNameLower}Manager) {
        this.${classNameLower}Manager = ${classNameLower}Manager;
    }

    /**
     * 创建${entityName}。
     *
     * @param request 创建${entityName}请求体
     * @return 创建成功后的${entityName}信息
     */
    @PostMapping
    public ${toolPrefix}Result<${className}Response> create(@RequestBody ${className}CreateRequest request) {
        return ${toolPrefix}Template.execute(request, new ${toolPrefix}Template.Callback<>() {

            @Override
            public void beforeService(${className}CreateRequest param) {
                ${className}ParamChecker.check${className}CreateRequest(param);
            }

            @Override
            public ${className}Response execute(${className}CreateRequest param) {
                ${className} ${classNameLower} = ${classNameLower}Manager.create${className}(${className}Assembler.toModel(param));
                return ${className}Assembler.toResponse(${classNameLower});
            }

            @Override
            public void afterService(${className}CreateRequest param, ${className}Response result) {
            }
        });
    }

    /**
     * 按 ID 查询${entityName}。
     *
     * @param id ${entityName} ID
     * @return ${entityName}信息
     */
    @GetMapping("/{id}")
    public ${toolPrefix}Result<${className}Response> get(@PathVariable Long id) {
        return ${toolPrefix}Template.execute(id, new ${toolPrefix}Template.Callback<>() {

            @Override
            public void beforeService(Long param) {
                ${className}ParamChecker.checkId(param);
            }

            @Override
            public ${className}Response execute(Long param) {
                ${className} ${classNameLower} = ${classNameLower}Manager.get${className}(param);
                ${toolPrefix}Invoker.throwErrWhenNull(${classNameLower}, ErrorCodeEnum.RESOURCE_NOT_FOUND, "${entityName}不存在");
                return ${className}Assembler.toResponse(${classNameLower});
            }

            @Override
            public void afterService(Long param, ${className}Response result) {
            }
        });
    }

    /**
     * 分页查询${entityName}。
     *
     * @param request 查询条件（含分页参数与时间区间）
     * @return 分页结果
     */
    @GetMapping("/page")
    public ${toolPrefix}Result<PageResult<${className}Response>> page(${className}QueryRequest request) {
        
        return ${toolPrefix}Template.execute(request, new ${toolPrefix}Template.Callback<>() {

            @Override
            public void beforeService(${className}QueryRequest param) {
                ${className}ParamChecker.check${className}QueryRequest(param);
            }

            @Override
            public PageResult<${className}Response> execute(${className}QueryRequest param) {
                param = ObjectUtil.defaultIfNull(param, new ${className}QueryRequest());
                PageResult<${className}> page = ${classNameLower}Manager.page${className}s(${className}Assembler.toQueryParam(param));
                return new PageResult<>(page.getTotal(), param.getPageNum(), param.getPageSize(),
                        page.getDataList().stream().map(${className}Assembler::toResponse).toList());
            }
        });
    }

    /**
     * 更新${entityName}（全量）。
     * 注意：PUT 为全量覆盖，未传字段会被置 NULL；部分更新请走 updateByCondition（Manager/DomainService）。
     *
     * @param id      ${entityName} ID
     * @param request 更新内容
     * @return 更新后的${entityName}信息
     */
    @PutMapping("/{id}")
    public ${toolPrefix}Result<Void> update(@PathVariable Long id, @RequestBody ${className}UpdateRequest request) {
        return ${toolPrefix}Template.executeWithoutResult(request, new ${toolPrefix}Template.CallbackWithoutResult<>() {

            @Override
            public void beforeService(${className}UpdateRequest param) {
                ${className}ParamChecker.checkId(id);
                ${className}ParamChecker.check${className}UpdateRequest(param);
            }

            @Override
            public void execute(${className}UpdateRequest param) {
                ${classNameLower}Manager.update${className}(${className}Assembler.toModel(param, id));
            }
        });
    }

    /**
     * 删除${entityName}。
     *
     * @param id ${entityName} ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ${toolPrefix}Result<Void> delete(@PathVariable Long id) {
        return ${toolPrefix}Template.executeWithoutResult(id, new ${toolPrefix}Template.CallbackWithoutResult<>() {

            @Override
            public void beforeService(Long id) {
                ${className}ParamChecker.checkId(id);
            }

            @Override
            public void execute(Long id) {
                ${classNameLower}Manager.delete${className}(id);
            }
        });
    }
}
