package ${basePackage}.app.web.assembler;

import ${basePackage}.app.web.dto.${className}CreateRequest;
import ${basePackage}.app.web.dto.${className}QueryRequest;
import ${basePackage}.app.web.dto.${className}Response;
import ${basePackage}.app.web.dto.${className}UpdateRequest;
import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.param.${className}QueryParam;

/**
 * ${tableComment}对象组装器：DTO 与领域模型互转，只存在于 web。
 */
public final class ${className}Assembler {

    private ${className}Assembler() {
    }

    public static ${className} toModel(${className}CreateRequest request) {
        ${className} ${classNameLower} = new ${className}();
<#list columns as c>
        ${classNameLower}.set${c.propertyName?cap_first}(request.${c.propertyName}());
</#list>        return ${classNameLower};
    }

    public static ${className} toModel(${className}UpdateRequest request, Long id) {
        ${className} ${classNameLower} = new ${className}();
        ${classNameLower}.setId(id);
<#list columns as c>
        ${classNameLower}.set${c.propertyName?cap_first}(request.${c.propertyName}());
</#list>        return ${classNameLower};
    }

    public static ${className}QueryParam toQueryParam(${className}QueryRequest request) {
        ${className}QueryParam param = new ${className}QueryParam();
<#list queryColumns as c>
        param.set${c.propertyName?cap_first}(request.get${c.propertyName?cap_first}());
</#list>        param.setPageNum(request.getPageNum() == null ? 1 : request.getPageNum());
        param.setCreateTimeBegin(request.getCreateTimeBegin());
        param.setCreateTimeEnd(request.getCreateTimeEnd());
        param.setUpdateTimeBegin(request.getUpdateTimeBegin());
        param.setUpdateTimeEnd(request.getUpdateTimeEnd());
        param.setPageSize(request.getPageSize() == null ? 10 : request.getPageSize());
        return param;
    }

    public static ${className}Response toResponse(${className} ${classNameLower}) {
        return new ${className}Response(
                ${classNameLower}.getId(),
<#list columns as c>                ${classNameLower}.get${c.propertyName?cap_first}(),
</#list>                ${classNameLower}.getCreateTime(),
                ${classNameLower}.getUpdateTime()
        );
    }
}
