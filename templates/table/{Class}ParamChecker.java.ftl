package ${basePackage}.web.checker;

import cn.hutool.core.util.ObjectUtil;
import ${basePackage}.common.util.tools.AssertUtil;
import ${basePackage}.common.util.tools.ParamValidator;
import ${basePackage}.core.model.enums.ErrorCodeEnum;
import ${basePackage}.web.param.${className}CreateRequest;
import ${basePackage}.web.param.${className}QueryRequest;
import ${basePackage}.web.param.${className}UpdateRequest;

/**
 * ${entityName}参数检查器。
 */
public final class ${className}ParamChecker {

    private ${className}ParamChecker() {
    }

    /**
     * 检查${entityName}创建参数。
     *
     * @param request ${entityName}创建请求
     */
    public static void check${className}CreateRequest(${className}CreateRequest request) {
        AssertUtil.throwErrWhenNull(request, ErrorCodeEnum.PARAM_INVALID, "创建参数不能为空");
        ParamValidator.validate(request);
    }

    /**
     * 检查${entityName}更新参数。
     *
     * @param request ${entityName}更新请求
     */
    public static void check${className}UpdateRequest(${className}UpdateRequest request) {
        AssertUtil.throwErrWhenNull(request, ErrorCodeEnum.PARAM_INVALID, "更新参数不能为空");
        ParamValidator.validate(request);
    }

    /**
     * 检查${entityName}主键参数（按主键查询/更新/删除共用）。
     *
     * @param ${pkCallArgs} ${entityName}主键
     */
    public static void ${pkCheckMethod}(${pkMethodArgs}) {
<#if compositePk>
<#list pkColumns as pk>
        AssertUtil.throwErrWhenNull(${pk.propertyName}, ErrorCodeEnum.PARAM_INVALID, "${pk.comment}不能为空");
</#list><#else>
        AssertUtil.throwErrWhenNull(id, ErrorCodeEnum.PARAM_INVALID, "${entityName}ID不能为空");
</#if>
    }

    /**
     * 检查${entityName}查询参数。
     *
     * @param request ${entityName}查询请求，可为 null（缺省分页）
     */
    public static void check${className}QueryRequest(${className}QueryRequest request) {
        if (ObjectUtil.isNull(request)) {
            return;
        }
        ParamValidator.validate(request);
    }
}
