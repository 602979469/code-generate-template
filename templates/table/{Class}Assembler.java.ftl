package ${basePackage}.web.assembler;

import cn.hutool.core.util.ObjectUtil;
import ${basePackage}.common.util.constant.PageConstants;
import ${basePackage}.core.model.domain.${className};
import ${basePackage}.core.model.param.${className}QueryParam;
import ${basePackage}.web.param.${className}CreateRequest;
import ${basePackage}.web.param.${className}QueryRequest;
import ${basePackage}.web.param.${className}UpdateRequest;
import ${basePackage}.web.result.${className}Response;

/**
 * ${entityName}对象组装器：DTO 与领域模型互转，只存在于 web。
 */
public final class ${className}Assembler {

    private ${className}Assembler() {
    }

    /**
     * 创建请求 DTO → 领域模型。
     *
     * @param request 创建${entityName}请求 DTO；为空返回 null
     * @return ${entityName}领域模型
     */
    public static ${className} toModel(${className}CreateRequest request) {
        if (request == null) {
            return null;
        }
        ${className} ${classNameLower} = new ${className}();
<#if !pkAuto && !compositePk>
        ${classNameLower}.set${pkPropertyName?cap_first}(request.get${pkPropertyName?cap_first}());
</#if>
<#list columns as c>
        ${classNameLower}.set${c.propertyName?cap_first}(request.get${c.propertyName?cap_first}());
</#list>        return ${classNameLower};
    }

    /**
     * 更新请求 DTO + 路径主键 → 领域模型。
     *
     * @param request 更新${entityName}请求 DTO；为空返回 null
     * @param ${pkCallArgs} 路径中的${entityName}主键
     * @return ${entityName}领域模型
     */
    public static ${className} toModel(${className}UpdateRequest request, ${pkMethodArgs}) {
        if (request == null) {
            return null;
        }
        ${className} ${classNameLower} = new ${className}();
<#if compositePk>
<#list pkColumns as pk>
        ${classNameLower}.set${pk.propertyName?cap_first}(${pk.propertyName});
</#list><#else>
        ${classNameLower}.set${pkPropertyName?cap_first}(id);
</#if>
<#list columns as c><#if !c.pk>
        ${classNameLower}.set${c.propertyName?cap_first}(request.get${c.propertyName?cap_first}());
</#if></#list>        return ${classNameLower};
    }

    /**
     * 查询请求 DTO → 查询参数。
     *
     * @param request ${entityName}查询请求 DTO；为空返回空查询参数（分页走默认值）
     * @return ${entityName}查询参数
     */
    public static ${className}QueryParam toQueryParam(${className}QueryRequest request) {
        if (request == null) {
            return new ${className}QueryParam();
        }
        ${className}QueryParam param = new ${className}QueryParam();
<#list queryColumns as c>
        param.set${c.propertyName?cap_first}(request.get${c.propertyName?cap_first}());
</#list>        param.setCreateTimeBegin(request.getCreateTimeBegin());
        param.setCreateTimeEnd(request.getCreateTimeEnd());
        param.setUpdateTimeBegin(request.getUpdateTimeBegin());
        param.setUpdateTimeEnd(request.getUpdateTimeEnd());
        param.setPageNum(ObjectUtil.defaultIfNull(request.getPageNum(), PageConstants.DEFAULT_PAGE_NUM));
        param.setPageSize(ObjectUtil.defaultIfNull(request.getPageSize(), PageConstants.DEFAULT_PAGE_SIZE));
        return param;
    }

    /**
     * 领域模型 → 响应 VO。
     *
     * @param ${classNameLower} ${entityName}领域模型；为空返回 null
     * @return ${entityName}响应 VO
     */
    public static ${className}Response toResponse(${className} ${classNameLower}) {
        if (${classNameLower} == null) {
            return null;
        }
        ${className}Response response = new ${className}Response();
<#if !compositePk>
        response.set${pkPropertyName?cap_first}(${classNameLower}.get${pkPropertyName?cap_first}());
</#if>
<#list columns as c><#if !c.sensitive>
        response.set${c.propertyName?cap_first}(${classNameLower}.get${c.propertyName?cap_first}());
</#if></#list>        response.setCreateTime(${classNameLower}.getCreateTime());
        response.setUpdateTime(${classNameLower}.getUpdateTime());
        return response;
    }
}
