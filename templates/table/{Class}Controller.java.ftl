package ${basePackage}.web.controller;

import cn.hutool.core.util.ObjectUtil;
import ${basePackage}.biz.service.${className}Manager;
import ${basePackage}.common.util.result.PageResult;
import ${basePackage}.common.util.tools.AssertUtil;
import ${basePackage}.common.util.tools.ConvertUtil;
import ${basePackage}.core.model.domain.${className};
import ${basePackage}.common.framework.enums.ErrorCodeEnum;
import ${basePackage}.web.assembler.${className}Assembler;
import ${basePackage}.web.checker.${className}ParamChecker;
import ${basePackage}.web.param.${className}CreateRequest;
import ${basePackage}.web.param.${className}QueryRequest;
import ${basePackage}.web.param.${className}UpdateRequest;
import ${basePackage}.web.result.ApiResult;
import ${basePackage}.web.result.${className}Response;
import ${basePackage}.web.template.ApiTemplate;
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
 * 参数校验、异常封装、请求日志与 Result 组装统一交给 ApiTemplate。
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
    public ApiResult<${className}Response> create(@RequestBody ${className}CreateRequest request) {
        return ApiTemplate.execute(request, new ApiTemplate.Callback<>() {

            @Override
            public void beforeService(${className}CreateRequest param) {
                ${className}ParamChecker.check${className}CreateRequest(param);
            }

            @Override
            public ${className}Response execute(${className}CreateRequest param) {
                ${className} ${classNameLower} = ${classNameLower}Manager.create${className}(${className}Assembler.toModel(param));
                return ${className}Assembler.toResponse(${classNameLower});
            }
        });
    }

    /**
     * 按主键查询${entityName}。
     *
     * @param ${pkCallArgs} ${entityName}主键
     * @return ${entityName}信息
     */
    @GetMapping("/${pkPathVars}")
    public ApiResult<${className}Response> get(${pkPathParams}) {
        return ApiTemplate.execute(${pkFirstArg}, new ApiTemplate.Callback<>() {

            @Override
            public void beforeService(${pkFirstType} param) {
                ${className}ParamChecker.${pkCheckMethod}(${pkCallArgs});
            }

            @Override
            public ${className}Response execute(${pkFirstType} param) {
                ${className} ${classNameLower} = ${classNameLower}Manager.get${className}(${pkCallArgs});
                AssertUtil.throwErrWhenNull(${classNameLower}, ErrorCodeEnum.RESOURCE_NOT_FOUND, "${entityName}不存在");
                return ${className}Assembler.toResponse(${classNameLower});
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
    public ApiResult<PageResult<${className}Response>> page(${className}QueryRequest request) {
        return ApiTemplate.execute(request, new ApiTemplate.Callback<>() {

            @Override
            public void beforeService(${className}QueryRequest param) {
                ${className}ParamChecker.check${className}QueryRequest(param);
            }

            @Override
            public PageResult<${className}Response> execute(${className}QueryRequest param) {
                param = ObjectUtil.defaultIfNull(param, new ${className}QueryRequest());
                PageResult<${className}> page = ${classNameLower}Manager.page${className}s(${className}Assembler.toQueryParam(param));
                return ConvertUtil.mapPage(page, ${className}Assembler::toResponse);
            }
        });
    }

    /**
     * 更新${entityName}（全量）。
     * 注意：PUT 为全量覆盖，未传字段会被置 NULL；部分更新请走 updateByCondition（Manager/DomainService）。
     *
     * @param ${pkCallArgs} ${entityName}主键
     * @param request 更新内容
     * @return 更新结果
     */
    @PutMapping("/${pkPathVars}")
    public ApiResult<Void> update(${pkPathParams}, @RequestBody ${className}UpdateRequest request) {
        return ApiTemplate.executeWithoutResult(request, new ApiTemplate.CallbackWithoutResult<>() {

            @Override
            public void beforeService(${className}UpdateRequest param) {
                ${className}ParamChecker.${pkCheckMethod}(${pkCallArgs});
                ${className}ParamChecker.check${className}UpdateRequest(param);
            }

            @Override
            public void execute(${className}UpdateRequest param) {
                ${classNameLower}Manager.update${className}(${className}Assembler.toModel(param, ${pkCallArgs}));
            }
        });
    }

    /**
     * 删除${entityName}。
     *
     * @param ${pkCallArgs} ${entityName}主键
     * @return 删除结果
     */
    @DeleteMapping("/${pkPathVars}")
    public ApiResult<Void> delete(${pkPathParams}) {
        return ApiTemplate.executeWithoutResult(${pkFirstArg}, new ApiTemplate.CallbackWithoutResult<${pkFirstType}>() {

            @Override
            public void beforeService(${pkFirstType} ${pkFirstArg}) {
                ${className}ParamChecker.${pkCheckMethod}(${pkCallArgs});
            }

            @Override
            public void execute(${pkFirstType} ${pkFirstArg}) {
                ${classNameLower}Manager.delete${className}(${pkCallArgs});
            }
        });
    }
}
