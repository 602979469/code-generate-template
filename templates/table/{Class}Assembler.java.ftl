package ${basePackage}.web.assembler;

import cn.hutool.core.util.ObjectUtil;
import ${basePackage}.web.param.${className}CreateRequest;
import ${basePackage}.web.param.${className}QueryRequest;
import ${basePackage}.web.param.${className}UpdateRequest;
import ${basePackage}.web.result.${className}Response;
import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.param.${className}QueryParam;

/**
 * ${entityName}对象组装器：DTO 与领域模型互转，只存在于 web。
 */
public final class ${className}Assembler {

    private ${className}Assembler() {
    }

    /**
     * 创建请求 DTO → 领域模型。
     *
     * @param request 创建${entityName}请求 DTO
     * @return ${entityName}领域模型
     */
    public static ${className} toModel(${className}CreateRequest request) {
        ${className} ${classNameLower} = new ${className}();
<#if !pkAuto>
        ${classNameLower}.set${pkPropertyName?cap_first}(request.get${pkPropertyName?cap_first}());
</#if>
<#list columns as c>
        ${classNameLower}.set${c.propertyName?cap_first}(request.get${c.propertyName?cap_first}());
</#list>        return ${classNameLower};
    }

    /**
     * 更新请求 DTO + 路径 ID → 领域模型。
     *
     * @param request 更新${entityName}请求 DTO
     * @param id      路径中的${entityName} ID
     * @return ${entityName}领域模型
     */
    public static ${className} toModel(${className}UpdateRequest request, ${pkJavaType} id) {
        ${className} ${classNameLower} = new ${className}();
        ${classNameLower}.set${pkPropertyName?cap_first}(id);
<#list columns as c>
        ${classNameLower}.set${c.propertyName?cap_first}(request.get${c.propertyName?cap_first}());
</#list>        return ${classNameLower};
    }

    /**
     * 查询请求 DTO → 查询参数。
     *
     * @param request ${entityName}查询请求 DTO
     * @return ${entityName}查询参数
     */
    public static ${className}QueryParam toQueryParam(${className}QueryRequest request) {
        ${className}QueryParam param = new ${className}QueryParam();
<#list queryColumns as c>
        param.set${c.propertyName?cap_first}(request.get${c.propertyName?cap_first}());
</#list>        param.setCreateTimeBegin(request.getCreateTimeBegin());
        param.setCreateTimeEnd(request.getCreateTimeEnd());
        param.setUpdateTimeBegin(request.getUpdateTimeBegin());
        param.setUpdateTimeEnd(request.getUpdateTimeEnd());
        param.setPageNum(ObjectUtil.defaultIfNull(request.getPageNum(), 1));
        param.setPageSize(ObjectUtil.defaultIfNull(request.getPageSize(), 10));
        return param;
    }

    /**
     * 领域模型 → 响应 VO。
     *
     * @param ${classNameLower} ${entityName}领域模型
     * @return ${entityName}响应 VO
     */
    public static ${className}Response toResponse(${className} ${classNameLower}) {
        ${className}Response response = new ${className}Response();
        response.set${pkPropertyName?cap_first}(${classNameLower}.get${pkPropertyName?cap_first}());
<#list columns as c>
        response.set${c.propertyName?cap_first}(${classNameLower}.get${c.propertyName?cap_first}());
</#list>        response.setCreateTime(${classNameLower}.getCreateTime());
        response.setUpdateTime(${classNameLower}.getUpdateTime());
        return response;
    }
}
