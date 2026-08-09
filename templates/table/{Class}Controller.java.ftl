package ${basePackage}.app.web;

import ${basePackage}.app.biz.${className}BizService;
import ${basePackage}.app.web.assembler.${className}Assembler;
import ${basePackage}.app.web.dto.${className}CreateRequest;
import ${basePackage}.app.web.dto.${className}QueryRequest;
import ${basePackage}.app.web.dto.${className}Response;
import ${basePackage}.app.web.dto.${className}UpdateRequest;
import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.result.PageResult;
import ${basePackage}.core.model.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ${tableComment}管理接口。Controller 只做参数校验、DTO 转换与结果包装，不含业务规则。
 */
@RestController
@RequestMapping("/api/v1/${classNameLower}s")
@Tag(name = "${tableComment}管理")
public class ${className}Controller {

    private final ${className}BizService ${classNameLower}BizService;

    public ${className}Controller(${className}BizService ${classNameLower}BizService) {
        this.${classNameLower}BizService = ${classNameLower}BizService;
    }

    @PostMapping
    public Result<${className}Response> create(@Valid @RequestBody ${className}CreateRequest request) {
        ${className} ${classNameLower} = ${classNameLower}BizService.create${className}(${className}Assembler.toModel(request));
        Result<${className}Response> result = new Result<>();
        result.setSuccess(true);
        result.setData(${className}Assembler.toResponse(${classNameLower}));
        return result;
    }

    @GetMapping("/{id}")
    public Result<${className}Response> get(@PathVariable Long id) {
        Result<${className}Response> result = new Result<>();
        result.setSuccess(true);
        result.setData(${className}Assembler.toResponse(${classNameLower}BizService.get${className}(id)));
        return result;
    }

    @GetMapping("/page")
    public Result<PageResult<${className}Response>> page(@Valid ${className}QueryRequest request) {
        PageResult<${className}> page = ${classNameLower}BizService.page${className}s(${className}Assembler.toQueryParam(request));
        PageResult<${className}Response> pageResult = new PageResult<>(page.getTotal(), request.getPageNum(), request.getPageSize(),
                page.getDataList().stream().map(${className}Assembler::toResponse).toList());
        Result<PageResult<${className}Response>> result = new Result<>();
        result.setSuccess(true);
        result.setData(pageResult);
        return result;
    }

    @PutMapping("/{id}")
    public Result<${className}Response> update(@PathVariable Long id, @Valid @RequestBody ${className}UpdateRequest request) {
        ${className} ${classNameLower} = ${classNameLower}BizService.update${className}(${className}Assembler.toModel(request, id));
        Result<${className}Response> result = new Result<>();
        result.setSuccess(true);
        result.setData(${className}Assembler.toResponse(${classNameLower}));
        return result;
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ${classNameLower}BizService.delete${className}(id);
        Result<Void> result = new Result<>();
        result.setSuccess(true);
        return result;
    }
}
