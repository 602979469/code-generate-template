package ${basePackage}.app.web.controller;

import ${basePackage}.app.biz.${className}BizService;
import ${basePackage}.app.web.assembler.${className}Assembler;
import ${basePackage}.app.web.param.${className}CreateRequest;
import ${basePackage}.app.web.param.${className}QueryRequest;
import ${basePackage}.app.web.param.${className}UpdateRequest;
import ${basePackage}.app.web.result.${className}Response;
import ${basePackage}.app.web.result.${toolPrefix}Result;
import ${basePackage}.app.web.template.${toolPrefix}Template;
import ${basePackage}.common.util.tools.${toolPrefix}ParamValidator;
import ${basePackage}.core.model.domain.${className};
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
 * ${tableComment}管理接口。Controller 只做参数校验、DTO 转换与结果包装，不含业务规则；
 * 参数校验、异常封装、请求日志与 Result 组装统一交给 ${toolPrefix}Template。
 */
@RestController
@RequestMapping("/api/v1/${classNameLower}s")
@Tag(name = "${tableComment}管理")
public class ${className}Controller {

    /** ${tableComment}业务服务。 */
    private final ${className}BizService ${classNameLower}BizService;

    public ${className}Controller(${className}BizService ${classNameLower}BizService) {
        this.${classNameLower}BizService = ${classNameLower}BizService;
    }

    /**
     * 创建${tableComment}。
     *
     * @param request 创建${tableComment}请求体
     * @return 创建成功后的${tableComment}信息
     */
    @PostMapping
    public ${toolPrefix}Result<${className}Response> create(@RequestBody ${className}CreateRequest request) {
        return ${toolPrefix}Template.execute(request, new ${toolPrefix}Template.Callback<${className}CreateRequest, ${className}Response>() {

            @Override
            public void beforeService(${className}CreateRequest param) {
                ${toolPrefix}ParamValidator.validate(param);
            }

            @Override
            public ${className}Response execute(${className}CreateRequest param) {
                ${className} ${classNameLower} = ${classNameLower}BizService.create${className}(${className}Assembler.toModel(param));
                return ${className}Assembler.toResponse(${classNameLower});
            }

            @Override
            public void afterService(${className}CreateRequest param, ${className}Response result) {
            }
        });
    }

    /**
     * 按 ID 查询${tableComment}。
     *
     * @param id ${tableComment} ID
     * @return ${tableComment}信息
     */
    @GetMapping("/{id}")
    public ${toolPrefix}Result<${className}Response> get(@PathVariable Long id) {
        return ${toolPrefix}Template.execute(id, new ${toolPrefix}Template.Callback<Long, ${className}Response>() {

            @Override
            public void beforeService(Long param) {
                ${toolPrefix}ParamValidator.validate(param);
            }

            @Override
            public ${className}Response execute(Long param) {
                return ${className}Assembler.toResponse(${classNameLower}BizService.get${className}(param));
            }

            @Override
            public void afterService(Long param, ${className}Response result) {
            }
        });
    }

    /**
     * 分页查询${tableComment}。
     *
     * @param request 查询条件（含分页参数与时间区间）
     * @return 分页结果
     */
    @GetMapping("/page")
    public ${toolPrefix}Result<PageResult<${className}Response>> page(${className}QueryRequest request) {
        return ${toolPrefix}Template.execute(request, new ${toolPrefix}Template.Callback<${className}QueryRequest, PageResult<${className}Response>>() {

            @Override
            public void beforeService(${className}QueryRequest param) {
                ${toolPrefix}ParamValidator.validate(param);
            }

            @Override
            public PageResult<${className}Response> execute(${className}QueryRequest param) {
                PageResult<${className}> page = ${classNameLower}BizService.page${className}s(${className}Assembler.toQueryParam(param));
                return new PageResult<>(page.getTotal(), param.getPageNum(), param.getPageSize(),
                        page.getDataList().stream().map(${className}Assembler::toResponse).toList());
            }

            @Override
            public void afterService(${className}QueryRequest param, PageResult<${className}Response> result) {
            }
        });
    }

    /**
     * 更新${tableComment}（全量）。
     *
     * @param id      ${tableComment} ID
     * @param request 更新内容
     * @return 更新后的${tableComment}信息
     */
    @PutMapping("/{id}")
    public ${toolPrefix}Result<${className}Response> update(@PathVariable Long id, @RequestBody ${className}UpdateRequest request) {
        return ${toolPrefix}Template.execute(request, new ${toolPrefix}Template.Callback<${className}UpdateRequest, ${className}Response>() {

            @Override
            public void beforeService(${className}UpdateRequest param) {
                ${toolPrefix}ParamValidator.validate(param);
            }

            @Override
            public ${className}Response execute(${className}UpdateRequest param) {
                ${className} ${classNameLower} = ${classNameLower}BizService.update${className}(${className}Assembler.toModel(param, id));
                return ${className}Assembler.toResponse(${classNameLower});
            }

            @Override
            public void afterService(${className}UpdateRequest param, ${className}Response result) {
            }
        });
    }

    /**
     * 删除${tableComment}。
     *
     * @param id ${tableComment} ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ${toolPrefix}Result<Void> delete(@PathVariable Long id) {
        return ${toolPrefix}Template.executeWithoutResult(id, new ${toolPrefix}Template.CallbackWithoutResult<Long>() {

            @Override
            public void beforeService(Long param) {
                ${toolPrefix}ParamValidator.validate(param);
            }

            @Override
            public void execute(Long param) {
                ${classNameLower}BizService.delete${className}(param);
            }

            @Override
            public void afterService(Long param) {
            }
        });
    }
}
